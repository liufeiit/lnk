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

import io.lnk.api.ServerConfiguration;
import io.lnk.api.ServiceGroup;
import io.lnk.flow.SemaphoreFlowController;
import io.lnk.lookup.LnkRegistry;
import io.lnk.port.DefaultServerPortAllocator;
import io.lnk.protocol.LnkProtocolFactorySelector;
import io.lnk.spring.core.DefaultServiceObjectFinder;
import io.lnk.spring.core.SpringLnkServer;
import io.lnk.spring.utils.LnkComponentParameterUtils;
import io.lnk.spring.utils.LnkComponentUtils;
import io.lnk.spring.utils.LnkComponentUtils.ComponentCallback;
import io.lnk.track.NestedTracker;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月23日 下午3:56:52
 */
public class LnkServerParser extends AbstractSingleBeanDefinitionParser {
    private static final Logger log = LoggerFactory.getLogger(LnkServerParser.class.getSimpleName());
    private static final String USE_EPOLL_NATIVE_SELECTOR_ATTR = "use-epoll-native-selector";
    private static final String DEFAULT_EXECUTOR_THREADS_ATTR = "default-executor-threads";
    private static final String DEFAULT_WORKER_PROCESSOR_THREADS_ATTR = "default-worker-processor-threads";
    private static final String POOLED_BYTEBUF_ALLOCATOR_ENABLE_ATTR = "pooled-bytebuf-allocator-enable";
    private static final String SOCKET_RCVBUF_SIZE_ATTR = "socket-rcvbuf-size";
    private static final String SOCKET_SNDBUF_SIZE_ATTR = "socket-sndbuf-size";
    private static final String CHANNEL_MAXIDLETIME_SECONDS_ATTR = "channel-maxidletime-seconds";
    private static final String SELECTOR_THREADS_ATTR = "selector-threads";
    private static final String WORKER_THREADS_ATTR = "worker-threads";
    private static final String PROTOCOL_ATTR = "protocol";
    private static final String LISTEN_PORT_ATTR = "listen-port";

    @Override
    protected Class<?> getBeanClass(Element element) {
        return SpringLnkServer.class;
    }

    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
        String serverId = this.resolveId(element);
        String serverPortAllocatorId = serverId + "DefaultServerPortAllocator";
        String protocolFactorySelectorId = serverId + "ProtocolFactorySelector";
        String serviceObjectFinderId = serverId + "DefaultServiceObjectFinder";
        String serverConfigurationId = serverId + "ServerConfiguration";

        builder.addPropertyValue("invoker", new RuntimeBeanReference(element.getAttribute("client")));
        LnkComponentUtils.parse(serverPortAllocatorId, DefaultServerPortAllocator.class, element, parserContext);
        builder.addPropertyValue("serverPortAllocator", new RuntimeBeanReference(serverPortAllocatorId));
        LnkComponentUtils.parse(protocolFactorySelectorId, LnkProtocolFactorySelector.class, element, parserContext);
        builder.addPropertyValue("protocolFactorySelector", new RuntimeBeanReference(protocolFactorySelectorId));
        LnkComponentUtils.parse(serviceObjectFinderId, DefaultServiceObjectFinder.class, element, parserContext);
        builder.addPropertyValue("serviceObjectFinder", new RuntimeBeanReference(serviceObjectFinderId));

        LnkComponentUtils.parse(serverConfigurationId, ServerConfiguration.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                String listenPort = element.getAttribute(LISTEN_PORT_ATTR);
                String protocol = element.getAttribute(PROTOCOL_ATTR);
                String workerThreads = element.getAttribute(WORKER_THREADS_ATTR);
                String selectorThreads = element.getAttribute(SELECTOR_THREADS_ATTR);
                String channelMaxIdleTimeSeconds = element.getAttribute(CHANNEL_MAXIDLETIME_SECONDS_ATTR);
                String socketSndBufSize = element.getAttribute(SOCKET_SNDBUF_SIZE_ATTR);
                String socketRcvBufSize = element.getAttribute(SOCKET_RCVBUF_SIZE_ATTR);
                String pooledByteBufAllocatorEnable = element.getAttribute(POOLED_BYTEBUF_ALLOCATOR_ENABLE_ATTR);
                String defaultWorkerProcessorThreads = element.getAttribute(DEFAULT_WORKER_PROCESSOR_THREADS_ATTR);
                String defaultExecutorThreads = element.getAttribute(DEFAULT_EXECUTOR_THREADS_ATTR);
                String useEpollNativeSelector = element.getAttribute(USE_EPOLL_NATIVE_SELECTOR_ATTR);

                beanDefinition.getPropertyValues().addPropertyValue("listenPort", listenPort);
                beanDefinition.getPropertyValues().addPropertyValue("protocol", protocol);
                beanDefinition.getPropertyValues().addPropertyValue("workerThreads", workerThreads);
                beanDefinition.getPropertyValues().addPropertyValue("selectorThreads", selectorThreads);
                beanDefinition.getPropertyValues().addPropertyValue("channelMaxIdleTimeSeconds", channelMaxIdleTimeSeconds);
                beanDefinition.getPropertyValues().addPropertyValue("socketSndBufSize", socketSndBufSize);
                beanDefinition.getPropertyValues().addPropertyValue("socketRcvBufSize", socketRcvBufSize);
                beanDefinition.getPropertyValues().addPropertyValue("pooledByteBufAllocatorEnable", pooledByteBufAllocatorEnable);
                beanDefinition.getPropertyValues().addPropertyValue("defaultWorkerProcessorThreads", defaultWorkerProcessorThreads);
                beanDefinition.getPropertyValues().addPropertyValue("defaultExecutorThreads", defaultExecutorThreads);
                beanDefinition.getPropertyValues().addPropertyValue("useEpollNativeSelector", useEpollNativeSelector);
            }
        });
        log.info("LnkServer[{}] configuration : {}", serverId, serverConfigurationId);
        builder.addPropertyValue("configuration", new RuntimeBeanReference(serverConfigurationId));

        List<Element> registryElements = DomUtils.getChildElementsByTagName(element, "registry");
        final Element registryElement = registryElements.get(0);
        String registryId = "lnkRegistry";
        LnkComponentUtils.parse(registryId, LnkRegistry.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, registryElement.getAttribute("address"));
                beanDefinition.getPropertyValues().addPropertyValues(LnkComponentParameterUtils.parse(registryElement));
            }
        });
        builder.addPropertyValue("registry", new RuntimeBeanReference(registryId));

        List<Element> flowControlElements = DomUtils.getChildElementsByTagName(element, "flow-control");
        if (CollectionUtils.isNotEmpty(flowControlElements)) {
            final Element flowControlElement = flowControlElements.get(0);
            final String permits = flowControlElement.getAttribute("permits");
            if (StringUtils.isNotBlank(permits)) {
                String flowControllerId = "flowController";
                LnkComponentUtils.parse(flowControllerId, SemaphoreFlowController.class, element, parserContext, new ComponentCallback() {
                    public void onParse(RootBeanDefinition beanDefinition) {
                        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, permits);
                        beanDefinition.getPropertyValues().addPropertyValues(LnkComponentParameterUtils.parse(flowControlElement));
                    }
                });
                builder.addPropertyValue("flowController", new RuntimeBeanReference(flowControllerId));
            }
        }

        List<Element> trackElements = DomUtils.getChildElementsByTagName(element, "tracker");
        if (CollectionUtils.isNotEmpty(trackElements)) {
            final Element trackerElement = trackElements.get(0);
            String trackerId = "tracker";
            LnkComponentUtils.parse(trackerId, NestedTracker.class, element, parserContext, new ComponentCallback() {
                public void onParse(RootBeanDefinition beanDefinition) {
                    beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, trackerElement.getAttribute("type"));
                    beanDefinition.getPropertyValues().addPropertyValues(LnkComponentParameterUtils.parse(trackerElement));
                }
            });
            builder.addPropertyValue("tracker", new RuntimeBeanReference(trackerId));
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
        log.info("parse LnkServer bean success.");
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        return this.resolveId(element);
    }

    private String resolveId(Element element) {
        return element.getAttribute(ID_ATTRIBUTE);
    }
}
