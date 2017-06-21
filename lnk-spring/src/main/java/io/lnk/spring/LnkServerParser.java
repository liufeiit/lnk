package io.lnk.spring;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import io.lnk.api.ServiceGroup;
import io.lnk.api.URI;
import io.lnk.api.track.Tracker;
import io.lnk.flow.SemaphoreFlowController;
import io.lnk.lookup.LnkRegistry;
import io.lnk.port.DefaultServerPortAllocator;
import io.lnk.protocol.LnkProtocolFactorySelector;
import io.lnk.remoting.RemotingProvider;
import io.lnk.remoting.ServerConfiguration;
import io.lnk.spring.core.DefaultServiceObjectFinder;
import io.lnk.spring.core.SpringLnkServer;
import io.lnk.spring.utils.BeanRegister;
import io.lnk.spring.utils.ParametersParser;
import io.lnk.track.LogTracker;

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
    private static final String PROVIDER_ATTR = "provider";
    private static final String LISTEN_PORT_ATTR = "listen-port";

    @Override
    protected Class<?> getBeanClass(Element element) {
        return SpringLnkServer.class;
    }

    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
        String serverId = this.resolveId(element);
        String serverPortAllocatorId = "defaultServerPortAllocator";
        String protocolFactorySelectorId = "serverProtocolFactorySelector";
        String serviceObjectFinderId = "defaultServiceObjectFinder";
        builder.addPropertyValue("invoker", new RuntimeBeanReference(element.getAttribute("client")));
        BeanRegister.register(serverPortAllocatorId, DefaultServerPortAllocator.class, element, parserContext);
        builder.addPropertyValue("serverPortAllocator", new RuntimeBeanReference(serverPortAllocatorId));
        BeanRegister.register(protocolFactorySelectorId, LnkProtocolFactorySelector.class, element, parserContext);
        builder.addPropertyValue("protocolFactorySelector", new RuntimeBeanReference(protocolFactorySelectorId));
        BeanRegister.register(serviceObjectFinderId, DefaultServiceObjectFinder.class, element, parserContext);
        builder.addPropertyValue("serviceObjectFinder", new RuntimeBeanReference(serviceObjectFinderId));

        String listenPort = element.getAttribute(LISTEN_PORT_ATTR);
        String provider = element.getAttribute(PROVIDER_ATTR);
        String workerThreads = element.getAttribute(WORKER_THREADS_ATTR);
        String selectorThreads = element.getAttribute(SELECTOR_THREADS_ATTR);
        String channelMaxidletimeSeconds = element.getAttribute(CHANNEL_MAXIDLETIME_SECONDS_ATTR);
        String socketSndbufSize = element.getAttribute(SOCKET_SNDBUF_SIZE_ATTR);
        String socketRcvbufSize = element.getAttribute(SOCKET_RCVBUF_SIZE_ATTR);
        String pooledBytebufAllocatorEnable = element.getAttribute(POOLED_BYTEBUF_ALLOCATOR_ENABLE_ATTR);
        String defaultWorkerProcessorThreads = element.getAttribute(DEFAULT_WORKER_PROCESSOR_THREADS_ATTR);
        String defaultExecutorThreads = element.getAttribute(DEFAULT_EXECUTOR_THREADS_ATTR);
        String useEpollNativeSelector = element.getAttribute(USE_EPOLL_NATIVE_SELECTOR_ATTR);
        ServerConfiguration configuration = new ServerConfiguration();
        int port = NumberUtils.toInt(listenPort, -1);
        if (port > 0) {
            configuration.setListenPort(port);
        }
        configuration.setProvider(RemotingProvider.valueOfProvider(provider));
        configuration.setWorkerThreads(NumberUtils.toInt(workerThreads, 10));
        configuration.setSelectorThreads(NumberUtils.toInt(selectorThreads, 5));
        configuration.setChannelMaxIdleTimeSeconds(NumberUtils.toInt(channelMaxidletimeSeconds, 120));
        configuration.setSocketSndBufSize(NumberUtils.toInt(socketSndbufSize, 65535));
        configuration.setSocketRcvBufSize(NumberUtils.toInt(socketRcvbufSize, 65535));
        configuration.setPooledByteBufAllocatorEnable(BooleanUtils.toBoolean(StringUtils.defaultString(pooledBytebufAllocatorEnable, "true")));
        configuration.setDefaultWorkerProcessorThreads(NumberUtils.toInt(defaultWorkerProcessorThreads, 10));
        configuration.setDefaultExecutorThreads(NumberUtils.toInt(defaultExecutorThreads, 8));
        configuration.setUseEpollNativeSelector(BooleanUtils.toBoolean(StringUtils.defaultString(useEpollNativeSelector, "false")));
        log.info("LnkServer[{}] configuration : {}", serverId, configuration);
        builder.addPropertyValue("configuration", configuration);

        List<Element> registryElements = DomUtils.getChildElementsByTagName(element, "registry");
        Element registryElement = registryElements.get(0);
        URI uri = URI.valueOf(registryElement.getAttribute("address"));
        uri = uri.addParameters(ParametersParser.parse(registryElement));
        builder.addPropertyValue("registry", new LnkRegistry(uri));

        List<Element> flowControlElements = DomUtils.getChildElementsByTagName(element, "flow-control");
        if (CollectionUtils.isNotEmpty(flowControlElements)) {
            Element flowControlElement = flowControlElements.get(0);
            String permitsString = StringUtils.defaultString(flowControlElement.getAttribute("permits"));
            int permits = NumberUtils.toInt(permitsString);
            if (StringUtils.isNotBlank(permitsString) && permits > 0) {
                SemaphoreFlowController flowController = new SemaphoreFlowController(permits);
                ParametersParser.wiredParameters(flowControlElement, flowController);
                builder.addPropertyValue("flowController", flowController);
            }
        }

        List<Element> trackElements = DomUtils.getChildElementsByTagName(element, "tracker");
        if (CollectionUtils.isNotEmpty(trackElements)) {
            Tracker tracker = null;
            Element trackerElement = trackElements.get(0);
            String trackType = StringUtils.defaultString(trackerElement.getAttribute("type"));
            if (StringUtils.equals(trackType, "logger")) {
                tracker = new LogTracker();
            } else {
                tracker = new LogTracker();
            }
            ParametersParser.wiredParameters(trackerElement, tracker);
            builder.addPropertyValue("tracker", tracker);
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
