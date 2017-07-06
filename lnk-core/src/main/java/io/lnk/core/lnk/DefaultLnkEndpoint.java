package io.lnk.core.lnk;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import io.lnk.api.Address;
import io.lnk.api.InvokerCommand;
import io.lnk.api.ServiceGroup;
import io.lnk.api.app.Application;
import io.lnk.api.cluster.LoadBalance;
import io.lnk.api.exception.LnkException;
import io.lnk.api.exception.LnkRejectException;
import io.lnk.api.exception.LnkTimeoutException;
import io.lnk.api.flow.FlowController;
import io.lnk.api.port.ServerPortAllocator;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.api.protocol.object.ObjectProtocolFactory;
import io.lnk.api.registry.Registry;
import io.lnk.api.utils.LnkThreadFactory;
import io.lnk.api.utils.NetUtils;
import io.lnk.core.LnkEndpoint;
import io.lnk.core.ServiceObjectFinder;
import io.lnk.remoting.CommandProcessor;
import io.lnk.remoting.Configuration;
import io.lnk.remoting.RemotingClient;
import io.lnk.remoting.RemotingServer;
import io.lnk.remoting.exception.RemotingConnectException;
import io.lnk.remoting.exception.RemotingSendRequestException;
import io.lnk.remoting.exception.RemotingTimeoutException;
import io.lnk.remoting.netty.NettyRemotingClient;
import io.lnk.remoting.netty.NettyRemotingServer;
import io.lnk.remoting.protocol.RemotingCommand;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月6日 上午10:20:15
 */
@SuppressWarnings("restriction")
public class DefaultLnkEndpoint implements LnkEndpoint {
    protected static final Logger log = LoggerFactory.getLogger(LnkEndpoint.class.getSimpleName());
    private Configuration configuration;
    private RemotingServer remotingServer;
    private RemotingClient remotingClient;
    private Registry registry;
    private LoadBalance loadBalance;
    private ServerPortAllocator serverPortAllocator;
    private Address serverAddress;
    private ProtocolFactorySelector protocolFactorySelector;
    private ServiceObjectFinder serviceObjectFinder;
    private FlowController flowController;
    private List<ServiceGroup> serviceGroups;
    private Application application;
    private ObjectProtocolFactory objectProtocolFactory;
    private AtomicBoolean started = new AtomicBoolean(false);
    private Semaphore multiCastSemaphore = new Semaphore(30, true);

    @Override
    public void start() {
        if (this.started.compareAndSet(false, true) == false) {
            log.info("LnkEndpoint is started.");
            return;
        }
        configuration.setListenPort(serverPortAllocator.selectPort(configuration.getListenPort(), application));
        remotingServer = new NettyRemotingServer(protocolFactorySelector, configuration);
        remotingServer.registerDefaultProcessor(this.createLnkCommandProcessor(), Executors.newFixedThreadPool(configuration.getDefaultWorkerProcessorThreads(), LnkThreadFactory.newThreadFactory("LnkEndpointWorkerProcessor-%d", false)));
        remotingServer.start();
        serverAddress = new Address(NetUtils.getLocalHost(), remotingServer.getServerAddress().getPort());
        if (CollectionUtils.isEmpty(serviceGroups) == false) {
            this.bind(serviceGroups.toArray(new ServiceGroup[serviceGroups.size()]));
        }
        remotingClient = new NettyRemotingClient(protocolFactorySelector, configuration);
        remotingClient.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    shutdown();
                } catch (Throwable e) {
                    log.error("LnkEndpoint shutdown Error.", e);
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
        log.info("LnkEndpoint '{}' start success bind {}", remotingServer, serverAddress);
        System.out.println(String.format("LnkEndpoint '%s' start success bind %s", remotingServer, serverAddress));
    }

    @Override
    public void registry(String serviceGroup, String serviceId, String version, int protocol, Object bean) throws LnkException {
        log.info("registry service serviceGroup : {}, serviceId : {}, version : {}, protocol : {}", new Object[] {serviceGroup, serviceId, version, protocol});
        this.serviceObjectFinder.registry(serviceGroup, serviceId, version, protocol, bean);
        this.registry.registry(serviceGroup, serviceId, version, protocol, serverAddress);
    }

    @Override
    public void unregistry(String serviceGroup, String serviceId, String version, int protocol) throws LnkException {
        log.info("unregistry service serviceGroup : {}, serviceId : {}, version : {}, protocol : {}", new Object[] {serviceGroup, serviceId, version, protocol});
        try {
            this.registry.unregistry(serviceGroup, serviceId, version, protocol, serverAddress);
        } catch (Throwable e) {
            log.warn("unregistry service serviceGroup : {}, serviceId : {}, version : {}, protocol : {} Error.", new Object[] {serviceGroup, serviceId, version, protocol});
        }
    }

    @Override
    public void bind(ServiceGroup... serviceGroups) {
        if (ArrayUtils.isEmpty(serviceGroups)) {
            log.info("bind serviceGroups is empty.");
            return;
        }
        for (ServiceGroup serviceGroup : serviceGroups) {
            int commandCode = serviceGroup.getServiceGroup().hashCode();
            this.remotingServer.registerProcessor(commandCode, this.createLnkCommandProcessor(), Executors.newFixedThreadPool(serviceGroup.getServiceGroupWorkerProcessorThreads(),
                    LnkThreadFactory.newThreadFactory("LnkEndpointWorkerProcessor[" + serviceGroup.getServiceGroup() + "]-%d", false)));
            log.info("bind serviceGroup {} success.", serviceGroup.getServiceGroup());
        }
    }

    @Override
    public InvokerCommand sync(InvokerCommand command, long timeoutMillis) throws LnkException, LnkTimeoutException {
        if (this.tryAcquireFailure(timeoutMillis)) {
            throw new LnkRejectException(command.commandSignature());
        }
        Address selectedAddr = null;
        try {
            long startMillis = System.currentTimeMillis();
            command.setIp(serverAddress.getHost());
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
    public void async(InvokerCommand command) throws LnkException, LnkTimeoutException {
        if (this.tryAcquireFailure(3000L)) {
            throw new LnkRejectException(command.commandSignature());
        }
        Address selectedAddr = null;
        try {
            long startMillis = System.currentTimeMillis();
            command.setIp(serverAddress.getHost());
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
    public void multicast(InvokerCommand command) {
        if (this.multicastTryAcquireFailure(3000L)) {
            throw new LnkRejectException(command.commandSignature());
        }
        try {
            long startMillis = System.currentTimeMillis();
            command.setIp(serverAddress.getHost());
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
    public boolean isStarted() {
        return this.started.get();
    }

    @Override
    public void shutdown() {
        if (this.started.compareAndSet(true, false) == false) {
            log.info("LnkEndpoint is shutdown.");
            return;
        }
        try {
            this.shutdown0();
        } catch (Throwable e) {
            log.error("shutdown LnkEndpoint Error.", e);
        }
        if (this.remotingClient != null) {
            this.remotingClient.shutdown();
            this.remotingClient = null;
        }
        if (remotingServer != null) {
            remotingServer.shutdown();
            remotingServer = null;
        }
        log.info("shutdown LnkEndpoint success.");
    }

    protected void shutdown0() throws Throwable {}

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

    protected final CommandProcessor createLnkCommandProcessor() {
        DefaultCommandProcessor processor = new DefaultCommandProcessor();
        processor.setProtocolFactorySelector(protocolFactorySelector);
        processor.setServiceObjectFinder(serviceObjectFinder);
        processor.setFlowController(flowController);
        processor.setObjectProtocolFactory(objectProtocolFactory);
        return processor;
    }
    
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
    
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
    
    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }
    
    public void setServerPortAllocator(ServerPortAllocator serverPortAllocator) {
        this.serverPortAllocator = serverPortAllocator;
    }
    
    public void setProtocolFactorySelector(ProtocolFactorySelector protocolFactorySelector) {
        this.protocolFactorySelector = protocolFactorySelector;
    }
    
    public void setServiceObjectFinder(ServiceObjectFinder serviceObjectFinder) {
        this.serviceObjectFinder = serviceObjectFinder;
    }
    
    public void setFlowController(FlowController flowController) {
        this.flowController = flowController;
    }
    
    public void setServiceGroups(List<ServiceGroup> serviceGroups) {
        this.serviceGroups = serviceGroups;
    }
    
    public void setApplication(Application application) {
        this.application = application;
    }
    
    public void setObjectProtocolFactory(ObjectProtocolFactory objectProtocolFactory) {
        this.objectProtocolFactory = objectProtocolFactory;
    }
}
