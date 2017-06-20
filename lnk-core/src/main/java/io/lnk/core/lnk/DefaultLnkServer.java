package io.lnk.core.lnk;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import io.lnk.api.Address;
import io.lnk.api.ServiceGroup;
import io.lnk.api.app.Application;
import io.lnk.api.exception.LnkException;
import io.lnk.api.flow.FlowController;
import io.lnk.api.port.ServerPortAllocator;
import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.api.registry.Registry;
import io.lnk.api.track.Tracker;
import io.lnk.core.CommandArgProtocolFactory;
import io.lnk.core.LnkServer;
import io.lnk.core.ServiceObjectFinder;
import io.lnk.core.processor.LnkCommandProcessor;
import io.lnk.core.protocol.LnkCommandArgProtocolFactory;
import io.lnk.remoting.CommandProcessor;
import io.lnk.remoting.RemotingServer;
import io.lnk.remoting.ServerConfiguration;
import io.lnk.remoting.mina.MinaRemotingServer;
import io.lnk.remoting.netty.NettyRemotingServer;
import io.lnk.remoting.utils.RemotingThreadFactory;
import io.lnk.remoting.utils.RemotingUtils;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午2:16:27
 */
@SuppressWarnings("restriction")
public class DefaultLnkServer implements LnkServer {
    protected static final Logger log = LoggerFactory.getLogger(LnkServer.class.getSimpleName());
    private ServerConfiguration configuration;
    private RemotingServer remotingServer;
    private Registry registry;
    private ServerPortAllocator serverPortAllocator;
    private Address serverAddress;
    private ProtocolFactorySelector protocolFactorySelector;
    private ServiceObjectFinder serviceObjectFinder;
    private FlowController flowController;
    private List<ServiceGroup> serviceGroups;
    private Application application;
    private Tracker tracker;
    private DefaultLnkInvoker invoker;
    private CommandArgProtocolFactory commandArgProtocolFactory;
    private AtomicBoolean started = new AtomicBoolean(false);

    @Override
    public final boolean isStarted() {
        return this.started.get();
    }

    @Override
    public final void start() {
        if (this.started.compareAndSet(false, true) == false) {
            log.info("LnkServer is started.");
            return;
        }
        this.commandArgProtocolFactory = new LnkCommandArgProtocolFactory(invoker.getRemoteObjectFactory());
        configuration.setListenPort(serverPortAllocator.selectPort(configuration.getListenPort(), application));
        switch (configuration.getProvider()) {
            case Netty:
                remotingServer = new NettyRemotingServer(protocolFactorySelector, configuration);
                break;
            case Mina:
                remotingServer = new MinaRemotingServer(protocolFactorySelector, configuration);
                break;
            default:
                throw new RuntimeException("unsupport RemotingServer provider : " + configuration.getProvider());
        }
        remotingServer.registerDefaultProcessor(this.createLnkCommandProcessor(),
                Executors.newFixedThreadPool(configuration.getDefaultWorkerProcessorThreads(), RemotingThreadFactory.newThreadFactory("LnkServerWorkerProcessor-%d", false)));
        remotingServer.start();
        serverAddress = new Address(RemotingUtils.getLocalAddress(), remotingServer.getServerAddress().getPort());
        if (CollectionUtils.isEmpty(serviceGroups) == false) {
            this.bind(serviceGroups.toArray(new ServiceGroup[serviceGroups.size()]));
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    DefaultLnkServer.this.shutdown();
                } catch (Throwable e) {
                    log.error("LnkServer shutdown Error.", e);
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
        log.info("LnkServer '{}' start success bind {}", remotingServer, serverAddress);
        System.out.println(String.format("LnkServer '%s' start success bind %s", remotingServer, serverAddress));
    }

    @Override
    public final void bind(ServiceGroup... serviceGroups) {
        if (ArrayUtils.isEmpty(serviceGroups)) {
            log.info("bind serviceGroups is empty.");
            return;
        }
        for (ServiceGroup serviceGroup : serviceGroups) {
            int commandCode = serviceGroup.getServiceGroup().hashCode();
            this.remotingServer.registerProcessor(commandCode, this.createLnkCommandProcessor(), Executors.newFixedThreadPool(serviceGroup.getServiceGroupWorkerProcessorThreads(),
                    RemotingThreadFactory.newThreadFactory("LnkServerWorkerProcessor[" + serviceGroup.getServiceGroup() + "]-%d", false)));
            log.info("bind serviceGroup {} success.", serviceGroup.getServiceGroup());
        }
    }

    @Override
    public final void registry(String serviceGroup, String serviceId, String version, int protocol, Object bean) throws LnkException {
        log.info("registry service serviceGroup : {}, serviceId : {}, version : {}, protocol : {}", new Object[] {serviceGroup, serviceId, version, protocol});
        this.serviceObjectFinder.registry(serviceGroup, serviceId, version, protocol, bean);
        this.registry.registry(serviceGroup, serviceId, version, protocol, serverAddress);
    }

    @Override
    public final void unregistry(String serviceGroup, String serviceId, String version, int protocol) throws LnkException {
        log.info("unregistry service serviceGroup : {}, serviceId : {}, version : {}, protocol : {}", new Object[] {serviceGroup, serviceId, version, protocol});
        try {
            this.registry.unregistry(serviceGroup, serviceId, version, protocol, serverAddress);
        } catch (Throwable e) {
            log.warn("unregistry service serviceGroup : {}, serviceId : {}, version : {}, protocol : {} Error.", new Object[] {serviceGroup, serviceId, version, protocol});
        }
    }

    protected final CommandProcessor createLnkCommandProcessor() {
        LnkCommandProcessor processor = new LnkCommandProcessor();
        processor.setProtocolFactorySelector(protocolFactorySelector);
        processor.setServiceObjectFinder(serviceObjectFinder);
        processor.setFlowController(flowController);
        processor.setApplication(application);
        processor.setTracker(tracker);
        processor.setCommandArgProtocolFactory(commandArgProtocolFactory);
        return processor;
    }

    @Override
    public final void shutdown() {
        if (this.started.compareAndSet(true, false) == false) {
          log.info("LnkServer is shutdown.");
            return;
        }
        try {
            this.shutdown0();
        } catch (Throwable e) {
            log.error("shutdown LnkServer Error.", e);
        }
        if (remotingServer != null) {
            remotingServer.shutdown();
            remotingServer = null;
        }
        log.info("shutdown LnkServer success.");
    }
    
    protected void shutdown0() throws Throwable {}

    public void setApplication(Application application) {
        this.application = application;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public void setConfiguration(ServerConfiguration configuration) {
        this.configuration = configuration;
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

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }
    
    public void setInvoker(DefaultLnkInvoker invoker) {
        this.invoker = invoker;
    }
}
