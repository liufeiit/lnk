package io.lnk.spring;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import io.lnk.api.ServiceGroup;
import io.lnk.api.app.Application;
import io.lnk.cluster.NestedLoadBalance;
import io.lnk.config.ctx.config.PlaceholderConfiguration;
import io.lnk.config.ctx.ns.NsRegistryImpl;
import io.lnk.core.caller.LnkRemoteObjectFactory;
import io.lnk.flow.SemaphoreFlowController;
import io.lnk.lookup.ZooKeeperRegistry;
import io.lnk.lookup.zookeeper.DefaultZooKeeperService;
import io.lnk.port.DefaultServerPortAllocator;
import io.lnk.protocol.LnkProtocolFactorySelector;
import io.lnk.protocol.object.LnkObjectProtocolFactory;
import io.lnk.remoting.Configuration;
import io.lnk.spring.core.DefaultServiceObjectFinder;
import io.lnk.spring.core.SpringLnkEndpoint;
import io.lnk.spring.utils.LnkComponentUtils;
import io.lnk.spring.utils.LnkComponentUtils.ComponentCallback;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月23日 下午3:56:52
 */
public class LnkEndpointParser extends AbstractSingleBeanDefinitionParser {
    private static final Logger log = LoggerFactory.getLogger(LnkEndpointParser.class.getSimpleName());
    private static final String USE_EPOLL_NATIVE_SELECTOR_ATTR = "use-epoll-native-selector";
    private static final String DEFAULT_EXECUTOR_THREADS_ATTR = "default-executor-threads";
    private static final String DEFAULT_WORKER_PROCESSOR_THREADS_ATTR = "default-worker-processor-threads";
    private static final String POOLED_BYTEBUF_ALLOCATOR_ENABLE_ATTR = "pooled-bytebuf-allocator-enable";
    private static final String SOCKET_RCVBUF_SIZE_ATTR = "socket-rcvbuf-size";
    private static final String SOCKET_SNDBUF_SIZE_ATTR = "socket-sndbuf-size";
    private static final String CONNECT_TIMEOUT_MILLIS_ATTR = "connect-timeout-millis";
    private static final String CHANNEL_MAXIDLETIME_SECONDS_ATTR = "channel-maxidletime-seconds";
    private static final String SELECTOR_THREADS_ATTR = "selector-threads";
    private static final String WORKER_THREADS_ATTR = "worker-threads";
    private static final String LISTEN_PORT_ATTR = "listen-port";

    @Override
    protected Class<?> getBeanClass(Element element) {
        return SpringLnkEndpoint.class;
    }

    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
        final String endpointId = this.resolveId(element);
        final String serverPortAllocatorId = "defaultServerPortAllocator";
        final String protocolFactorySelectorId = "lnkProtocolFactorySelector";
        final String serviceObjectFinderId = "defaultServiceObjectFinder";
        final String configurationId = "lnkConfiguration";
        final String remoteObjectFactoryId = "lnkRemoteObjectFactory";
        final String objectProtocolFactoryId = "lnkObjectProtocolFactory";
        final String applicationId = "lnkApplication";
        final String nsRegistryId = "nsRegistry";
        final String registryId = "lnkRegistry";
        final String zooKeeperServiceId = "zooKeeperService";
        final String loadBalanceId = "nestedLoadBalance";
        final Element applicationElement = DomUtils.getChildElementByTagName(element, "application");
        LnkComponentUtils.parse(applicationId, Application.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("app", applicationElement.getAttribute("app"));
                beanDefinition.getPropertyValues().addPropertyValue("type", applicationElement.getAttribute("type"));
            }
        });
        builder.addPropertyValue("application", new RuntimeBeanReference(applicationId));
        LnkComponentUtils.parse(PlaceholderConfiguration.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("systemId", applicationElement.getAttribute("app"));
            }
        });
        LnkComponentUtils.parse(nsRegistryId, NsRegistryImpl.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("nsHome", applicationElement.getAttribute("ns-home"));
            }
        });
        builder.addPropertyValue("nsRegistry", new RuntimeBeanReference(nsRegistryId));
        LnkComponentUtils.parse(objectProtocolFactoryId, LnkObjectProtocolFactory.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("remoteObjectFactory", new RuntimeBeanReference(remoteObjectFactoryId));
            }
        });
        builder.addPropertyValue("objectProtocolFactory", new RuntimeBeanReference(objectProtocolFactoryId));
        LnkComponentUtils.parse(remoteObjectFactoryId, LnkRemoteObjectFactory.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("endpoint", new RuntimeBeanReference(endpointId));
                beanDefinition.getPropertyValues().addPropertyValue("protocolFactorySelector", new RuntimeBeanReference(protocolFactorySelectorId));
                beanDefinition.getPropertyValues().addPropertyValue("objectProtocolFactory", new RuntimeBeanReference(objectProtocolFactoryId));
            }
        });
        builder.addPropertyValue("remoteObjectFactory", new RuntimeBeanReference(remoteObjectFactoryId));
        LnkComponentUtils.parse(serverPortAllocatorId, DefaultServerPortAllocator.class, element, parserContext);
        builder.addPropertyValue("serverPortAllocator", new RuntimeBeanReference(serverPortAllocatorId));
        LnkComponentUtils.parse(protocolFactorySelectorId, LnkProtocolFactorySelector.class, element, parserContext);
        builder.addPropertyValue("protocolFactorySelector", new RuntimeBeanReference(protocolFactorySelectorId));
        LnkComponentUtils.parse(serviceObjectFinderId, DefaultServiceObjectFinder.class, element, parserContext);
        builder.addPropertyValue("serviceObjectFinder", new RuntimeBeanReference(serviceObjectFinderId));
        LnkComponentUtils.parse(configurationId, Configuration.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("listenPort", element.getAttribute(LISTEN_PORT_ATTR));
                beanDefinition.getPropertyValues().addPropertyValue("workerThreads", element.getAttribute(WORKER_THREADS_ATTR));
                beanDefinition.getPropertyValues().addPropertyValue("selectorThreads", element.getAttribute(SELECTOR_THREADS_ATTR));
                beanDefinition.getPropertyValues().addPropertyValue("connectTimeoutMillis", element.getAttribute(CONNECT_TIMEOUT_MILLIS_ATTR));
                beanDefinition.getPropertyValues().addPropertyValue("channelMaxIdleTimeSeconds", element.getAttribute(CHANNEL_MAXIDLETIME_SECONDS_ATTR));
                beanDefinition.getPropertyValues().addPropertyValue("socketSndBufSize", element.getAttribute(SOCKET_SNDBUF_SIZE_ATTR));
                beanDefinition.getPropertyValues().addPropertyValue("socketRcvBufSize", element.getAttribute(SOCKET_RCVBUF_SIZE_ATTR));
                beanDefinition.getPropertyValues().addPropertyValue("pooledByteBufAllocatorEnable", element.getAttribute(POOLED_BYTEBUF_ALLOCATOR_ENABLE_ATTR));
                beanDefinition.getPropertyValues().addPropertyValue("defaultWorkerProcessorThreads", element.getAttribute(DEFAULT_WORKER_PROCESSOR_THREADS_ATTR));
                beanDefinition.getPropertyValues().addPropertyValue("defaultExecutorThreads", element.getAttribute(DEFAULT_EXECUTOR_THREADS_ATTR));
                beanDefinition.getPropertyValues().addPropertyValue("useEpollNativeSelector", element.getAttribute(USE_EPOLL_NATIVE_SELECTOR_ATTR));
            }
        });
        log.info("LnkEndpoint[{}] configuration : {}", endpointId, configurationId);
        builder.addPropertyValue("configuration", new RuntimeBeanReference(configurationId));
        List<Element> registryElements = DomUtils.getChildElementsByTagName(element, "registry");
        final Element registryElement = registryElements.get(0);
        LnkComponentUtils.parse(zooKeeperServiceId, DefaultZooKeeperService.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("zookeeperUri", registryElement.getAttribute("address"));
            }
        });
        LnkComponentUtils.parse(registryId, ZooKeeperRegistry.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("zooKeeperService", new RuntimeBeanReference(zooKeeperServiceId));
            }
        });
        builder.addPropertyValue("registry", new RuntimeBeanReference(registryId));
        List<Element> loadBalanceElements = DomUtils.getChildElementsByTagName(element, "load-balance");
        final Element loadBalanceElement = loadBalanceElements.get(0);
        LnkComponentUtils.parse(loadBalanceId, NestedLoadBalance.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, loadBalanceElement.getAttribute("type"));
            }
        });
        builder.addPropertyValue("loadBalance", new RuntimeBeanReference(loadBalanceId));
        List<Element> flowControlElements = DomUtils.getChildElementsByTagName(element, "flow-control");
        if (CollectionUtils.isNotEmpty(flowControlElements)) {
            final Element flowControlElement = flowControlElements.get(0);
            final String permits = flowControlElement.getAttribute("permits");
            if (StringUtils.isNotBlank(permits)) {
                String flowControllerId = "flowController";
                LnkComponentUtils.parse(flowControllerId, SemaphoreFlowController.class, element, parserContext, new ComponentCallback() {
                    public void onParse(RootBeanDefinition beanDefinition) {
                        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, permits);
                    }
                });
                builder.addPropertyValue("flowController", new RuntimeBeanReference(flowControllerId));
            }
        }
        List<Element> bindElements = DomUtils.getChildElementsByTagName(element, "bind");
        Element bindElement = bindElements.get(0);
        List<Element> serviceGroupElements = DomUtils.getChildElementsByTagName(bindElement, "service-group");
        List<ServiceGroup> serviceGroups = new ArrayList<ServiceGroup>();
        for (Element serviceGroupElement : serviceGroupElements) {
            ServiceGroup serviceGroup = new ServiceGroup();
            serviceGroup.setServiceGroup(StringUtils.trimToEmpty(serviceGroupElement.getAttribute("service-group")));
            serviceGroup.setServiceGroupWorkerProcessorThreads(NumberUtils.toInt(serviceGroupElement.getAttribute(WORKER_THREADS_ATTR), 10));
            serviceGroups.add(serviceGroup);
        }
        builder.addPropertyValue("serviceGroups", serviceGroups);
        log.info("build LnkEndpoint named {} success.", endpointId);
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        return this.resolveId(element);
    }

    private String resolveId(Element element) {
        return element.getAttribute(ID_ATTRIBUTE);
    }
}
