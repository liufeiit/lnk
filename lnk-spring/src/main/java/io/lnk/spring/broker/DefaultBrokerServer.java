package io.lnk.spring.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

import io.lnk.api.Address;
import io.lnk.api.ServerConfiguration;
import io.lnk.api.app.Application;
import io.lnk.api.broker.BrokerCaller;
import io.lnk.api.broker.BrokerCallerAware;
import io.lnk.api.broker.BrokerProvider;
import io.lnk.api.broker.BrokerServer;
import io.lnk.api.port.ServerPortAllocator;
import io.lnk.api.utils.NetUtils;
import io.lnk.broker.http.HttpBrokerServer;
import io.lnk.broker.ws.WsBrokerServer;
import io.lnk.spring.core.LnkApplication;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月22日 上午11:21:37
 */
@SuppressWarnings("restriction")
public class DefaultBrokerServer implements BrokerCallerAware, BeanFactoryAware, InitializingBean {
    protected static final Logger log = LoggerFactory.getLogger(DefaultBrokerServer.class.getSimpleName());
    private BeanFactory beanFactory;
    private BrokerServer brokerServer;
    private ServerPortAllocator serverPortAllocator;
    private ServerConfiguration configuration;
    private BrokerCaller caller;
    private Address serverAddress;
    private Application application;

    @Override
    public void afterPropertiesSet() throws Exception {
        LnkApplication lnkApplication = this.beanFactory.getBean(LnkApplication.LNK_APPLICATION_NAME, LnkApplication.class);
        this.application = lnkApplication.getApplication();
        configuration.setListenPort(serverPortAllocator.selectPort(configuration.getListenPort(), application));
        BrokerProvider brokerProvider = BrokerProvider.valueOfProvider(configuration.getProvider());
        switch (brokerProvider) {
            case HTTP:
                brokerServer = new HttpBrokerServer(configuration);
                break;
            case WS:
                brokerServer = new WsBrokerServer(configuration);
                break;
            default:
                throw new RuntimeException("unsupport BrokerServer provider : " + configuration.getProvider());
        }
        brokerServer.setBrokerCaller(caller);
        brokerServer.start();
        serverAddress = new Address(NetUtils.getLocalAddress().getHostAddress(), brokerServer.getServerAddress().getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    DefaultBrokerServer.this.shutdown();
                } catch (Throwable e) {
                    log.error("BrokerServer shutdown Error.", e);
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
        log.info("BrokerServer '{}' start success bind {}", brokerServer, serverAddress);
        System.out.println(String.format("BrokerServer '%s' start success bind %s", brokerServer, serverAddress));
    }

    public void shutdown() {
        if (brokerServer != null) {
            brokerServer.shutdown();
            brokerServer = null;
        }
        log.info("shutdown BrokerServer success.");
    }

    @Override
    public void setBrokerCaller(BrokerCaller caller) {
        this.caller = caller;
    }

    public void setConfiguration(ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setServerPortAllocator(ServerPortAllocator serverPortAllocator) {
        this.serverPortAllocator = serverPortAllocator;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
