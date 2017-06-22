package io.lnk.core.lnk;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.api.Address;
import io.lnk.api.ClientConfiguration;
import io.lnk.api.InvokerCallback;
import io.lnk.api.InvokerCommand;
import io.lnk.api.RemoteObjectFactory;
import io.lnk.api.app.Application;
import io.lnk.api.broker.BrokerCaller;
import io.lnk.api.cluster.LoadBalance;
import io.lnk.api.exception.LnkException;
import io.lnk.api.exception.LnkRejectException;
import io.lnk.api.exception.LnkTimeoutException;
import io.lnk.api.flow.FlowController;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.api.registry.Registry;
import io.lnk.api.utils.NetUtils;
import io.lnk.core.LnkInvoker;
import io.lnk.remoting.RemotingCallback;
import io.lnk.remoting.RemotingClient;
import io.lnk.remoting.RemotingProvider;
import io.lnk.remoting.ReplyFuture;
import io.lnk.remoting.exception.RemotingConnectException;
import io.lnk.remoting.exception.RemotingSendRequestException;
import io.lnk.remoting.exception.RemotingTimeoutException;
import io.lnk.remoting.mina.MinaRemotingClient;
import io.lnk.remoting.netty.NettyRemotingClient;
import io.lnk.remoting.protocol.RemotingCommand;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午2:16:51
 */
@SuppressWarnings("restriction")
public class DefaultLnkInvoker implements LnkInvoker {
    protected static final Logger log = LoggerFactory.getLogger(LnkInvoker.class.getSimpleName());
    protected RemoteObjectFactory remoteObjectFactory;
    protected BrokerCaller brokerCaller;
    private String ip;
    private ClientConfiguration configuration;
    private RemotingClient remotingClient;
    private Registry registry;
    private LoadBalance loadBalance;
    private ProtocolFactorySelector protocolFactorySelector;
    private Application application;
    private FlowController flowController;
    private Semaphore multiCastSemaphore = new Semaphore(30, true);
    private AtomicBoolean started = new AtomicBoolean(false);

    @Override
    public final boolean isStarted() {
        return this.started.get();
    }

    @Override
    public final void start() {
        if (this.started.compareAndSet(false, true) == false) {
            log.info("LnkInvoker is started.");
            return;
        }
        this.ip = NetUtils.getLocalAddress().getHostAddress();
        RemotingProvider remotingProvider = RemotingProvider.valueOfProvider(configuration.getProvider());
        switch (remotingProvider) {
            case Netty:
                remotingClient = new NettyRemotingClient(protocolFactorySelector, configuration);
                break;
            case Mina:
                remotingClient = new MinaRemotingClient(protocolFactorySelector, configuration);
                break;
            default:
                throw new RuntimeException("unsupport RemotingClient provider : " + configuration.getProvider());
        }
        remotingClient.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    DefaultLnkInvoker.this.shutdown();
                } catch (Throwable e) {
                    log.error("LnkInvoker shutdown Error.", e);
                }
            }
        }));
        SignalHandler sh = new SignalHandler() {
            public void handle(Signal sn) {
                log.info(sn.getName() + " signal is recevied.");
                System.exit(0);
            }
        };
        try {
            Signal.handle(new Signal("HUP"), sh);
        } catch (Throwable e) {
            log.warn("Signal handle HUP Error.");
        }
        try {
            Signal.handle(new Signal("INT"), sh);
        } catch (Throwable e) {
            log.warn("Signal handle INT Error.");
        }
        try {
            Signal.handle(new Signal("TERM"), sh);
        } catch (Throwable e) {
            log.warn("Signal handle TERM Error.");
        }
        log.info("LnkInvoker '{}' start success.", remotingClient);
        System.out.println(String.format("LnkInvoker '%s' start success.", remotingClient));
    }

    @Override
    public final InvokerCommand sync(final InvokerCommand command, final long timeoutMillis) throws LnkException, LnkTimeoutException {
        if (this.tryAcquireFailure(timeoutMillis)) {
            throw new LnkRejectException(command.commandSignature());
        }
        Address selectedAddr = null;
        try {
            long startMillis = System.currentTimeMillis();
            command.setIp(ip);
            command.setApplication(application);
            int commandCode = command.getServiceGroup().hashCode();
            ProtocolFactory protocolFactory = protocolFactorySelector.select(command.getProtocol());
            RemotingCommand request = new RemotingCommand();
            request.setCode(commandCode);
            request.setProtocol(command.getProtocol());
            request.setBody(protocolFactory.encode(command));
            Address[] candidates = registry.lookup(command.getServiceGroup(), command.getServiceId(), command.getVersion(), command.getProtocol());
            selectedAddr = loadBalance.select(command, candidates);
            RemotingCommand response = remotingClient.invokeSync(selectedAddr.toString(), request, timeoutMillis);
            if (commandCode == response.getCode()) {
                InvokerCommand invokerCommand = protocolFactory.decode(InvokerCommand.class, response.getBody());
                long endMillis = System.currentTimeMillis();
                log.info("invoker sync correlationId<{}>, serviceId<{}>, used {}(ms) success.", new Object[] {command.getId(), command.commandSignature(), (endMillis - startMillis)});
                return invokerCommand;
            }
            log.error("invoker sync correlationId<" + command.getId() + ">, serviceId<{}>, code<{}> Error.", new Object[] {command.commandSignature(), response.getCode()});
            throw new LnkException("invoker sync correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + ">, code<" + response.getCode() + "> Error.");
        } catch (RemotingConnectException e) {
            log.error("invoker sync correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
            registry.unregistry(command.getServiceGroup(), command.getServiceId(), command.getVersion(), command.getProtocol(), selectedAddr);
            throw new LnkException("invoker sync correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
        } catch (RemotingSendRequestException e) {
            log.error("invoker sync correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
            throw new LnkException("invoker sync correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
        } catch (RemotingTimeoutException e) {
            log.error("invoker sync correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> timeout " + e.getLocalizedMessage(), e);
            throw new LnkTimeoutException("invoker sync correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> timeout " + e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            log.error("invoker sync correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
            throw new LnkException("invoker sync correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
        } finally {
            this.release();
        }
    }

    @Override
    public final void async_callback(final InvokerCommand command, final long timeoutMillis, final InvokerCallback callback) throws LnkException, LnkTimeoutException {
        if (this.tryAcquireFailure(timeoutMillis)) {
            throw new LnkRejectException(command.commandSignature());
        }
        Address selectedAddr = null;
        try {
            final long startMillis = System.currentTimeMillis();
            command.setIp(ip);
            command.setApplication(application);
            final int commandCode = command.getServiceGroup().hashCode();
            final ProtocolFactory protocolFactory = protocolFactorySelector.select(command.getProtocol());
            RemotingCommand request = new RemotingCommand();
            request.setCode(commandCode);
            request.setProtocol(command.getProtocol());
            request.setBody(protocolFactory.encode(command));
            Address[] candidates = registry.lookup(command.getServiceGroup(), command.getServiceId(), command.getVersion(), command.getProtocol());
            selectedAddr = loadBalance.select(command, candidates);
            remotingClient.invokeAsync(selectedAddr.toString(), request, timeoutMillis, new RemotingCallback() {
                @Override
                public void onComplete(ReplyFuture replyFuture) {
                    if (replyFuture.getResponse() == null) {
                        callback.onError(new LnkException("Can't found RemotingCommand response from ReplyFuture."));
                        return;
                    }
                    RemotingCommand response = replyFuture.getResponse();
                    if (commandCode == response.getCode()) {
                        InvokerCommand invokerCommand = protocolFactory.decode(InvokerCommand.class, response.getBody());
                        long endMillis = System.currentTimeMillis();
                        log.info("invoker async_callback correlationId<{}>, serviceId<{}>, used {}(ms) success.",
                                new Object[] {command.getId(), command.commandSignature(), (endMillis - startMillis)});
                        callback.onComplete(invokerCommand);
                        return;
                    }
                    callback.onError(new LnkException(
                            "invoker async_callback correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + ">, code<" + response.getCode() + "> Error."));
                }
            });
        } catch (RemotingConnectException e) {
            log.error("invoker async_callback correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
            registry.unregistry(command.getServiceGroup(), command.getServiceId(), command.getVersion(), command.getProtocol(), selectedAddr);
            throw new LnkException("invoker async_callback correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
        } catch (RemotingSendRequestException e) {
            log.error("invoker async_callback correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
            throw new LnkException("invoker async_callback correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            log.error("invoker async_callback correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
            throw new LnkException("invoker async_callback correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
        } finally {
            this.release();
        }
    }

    @Override
    public final void async(InvokerCommand command) throws LnkException, LnkTimeoutException {
        if (this.tryAcquireFailure(3000L)) {
            throw new LnkRejectException(command.commandSignature());
        }
        Address selectedAddr = null;
        try {
            long startMillis = System.currentTimeMillis();
            command.setIp(ip);
            command.setApplication(application);
            int commandCode = command.getServiceGroup().hashCode();
            ProtocolFactory protocolFactory = protocolFactorySelector.select(command.getProtocol());
            RemotingCommand request = new RemotingCommand();
            request.setCode(commandCode);
            request.setProtocol(command.getProtocol());
            request.setBody(protocolFactory.encode(command));
            Address[] candidates = registry.lookup(command.getServiceGroup(), command.getServiceId(), command.getVersion(), command.getProtocol());
            selectedAddr = loadBalance.select(command, candidates);
            remotingClient.invokeOneway(selectedAddr.toString(), request);
            long endMillis = System.currentTimeMillis();
            log.info("invoker async correlationId<{}>, serviceId<{}>, used {}(ms) success.", new Object[] {command.getId(), command.commandSignature(), (endMillis - startMillis)});
        } catch (RemotingConnectException e) {
            log.error("invoker async correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
            registry.unregistry(command.getServiceGroup(), command.getServiceId(), command.getVersion(), command.getProtocol(), selectedAddr);
            throw new LnkException("invoker async correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
        } catch (RemotingSendRequestException e) {
            log.error("invoker async correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
            throw new LnkException("invoker async correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            log.error("invoker async correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
            throw new LnkException("invoker async correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
        } finally {
            this.release();
        }
    }

    @Override
    public final void multicast(InvokerCommand command) {
        if (this.multicastTryAcquireFailure(3000L)) {
            throw new LnkRejectException(command.commandSignature());
        }
        try {
            long startMillis = System.currentTimeMillis();
            command.setIp(ip);
            command.setApplication(application);
            int commandCode = command.getServiceGroup().hashCode();
            ProtocolFactory protocolFactory = protocolFactorySelector.select(command.getProtocol());
            RemotingCommand request = new RemotingCommand();
            request.setCode(commandCode);
            request.setProtocol(command.getProtocol());
            request.setBody(protocolFactory.encode(command));
            Address[] addrList = registry.lookup(command.getServiceGroup(), command.getServiceId(), command.getVersion(), command.getProtocol());
            if (ArrayUtils.isNotEmpty(addrList)) {
                for (Address address : addrList) {
                    try {
                        remotingClient.invokeOneway(address.toString(), request);
                    } catch (Throwable e) {
                        if (e instanceof RemotingConnectException) {
                            registry.unregistry(command.getServiceGroup(), command.getServiceId(), command.getVersion(), command.getProtocol(), address);
                        }
                        log.error("invoker async multicast correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> " + e.getLocalizedMessage(), e);
                    }
                }
            }
            long endMillis = System.currentTimeMillis();
            log.info("invoker async multicast correlationId<{}>, serviceId<{}>, used {}(ms) success.", new Object[] {command.getId(), command.commandSignature(), (endMillis - startMillis)});
        } finally {
            this.releaseMulticast();
        }
    }

    @Override
    public final void shutdown() {
        if (this.started.compareAndSet(true, false) == false) {
            log.info("LnkInvoker is shutdown.");
            return;
        }
        if (this.remotingClient != null) {
            this.remotingClient.shutdown();
            this.remotingClient = null;
        }
        log.info("shutdown LnkInvoker success.");
    }

    private final boolean multicastTryAcquireFailure(long timeoutMillis) {
        try {
            return !this.multiCastSemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            log.error("Multicast tryAcquire Error.", e);
        }
        return true;
    }

    private final void releaseMulticast() {
        this.multiCastSemaphore.release();
    }

    private final boolean tryAcquireFailure(long timeoutMillis) {
        if (this.flowController == null) {
            return false;
        }
        return this.flowController.tryAcquireFailure(timeoutMillis);
    }

    private final void release() {
        if (this.flowController == null) {
            return;
        }
        this.flowController.release();
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void setConfiguration(ClientConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    public void setProtocolFactorySelector(ProtocolFactorySelector protocolFactorySelector) {
        this.protocolFactorySelector = protocolFactorySelector;
    }

    public void setFlowController(FlowController flowController) {
        this.flowController = flowController;
    }
    
    public void setRemoteObjectFactory(RemoteObjectFactory remoteObjectFactory) {
        this.remoteObjectFactory = remoteObjectFactory;
    }
    
    public void setBrokerCaller(BrokerCaller brokerCaller) {
        this.brokerCaller = brokerCaller;
    }

    public RemoteObjectFactory getRemoteObjectFactory() {
        return remoteObjectFactory;
    }
}
