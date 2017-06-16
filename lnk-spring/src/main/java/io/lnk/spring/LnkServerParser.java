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
import org.springframework.beans.factory.config.RuntimeBeanReference;
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
import io.lnk.remoting.netty.NettyServerConfiguration;
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

    @Override
    protected Class<?> getBeanClass(Element element) {
        return SpringLnkServer.class;
    }

    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
        builder.addPropertyValue("invoker", new RuntimeBeanReference(element.getAttribute("client")));
        builder.addPropertyValue("serverPortAllocator", new DefaultServerPortAllocator());
        
        LnkProtocolFactorySelector protocolFactorySelector = new LnkProtocolFactorySelector();
        builder.addPropertyValue("protocolFactorySelector", protocolFactorySelector);
        
        NettyServerConfiguration configuration = new NettyServerConfiguration();
        int port = NumberUtils.toInt(element.getAttribute("listen-port"), -1);
        if (port > 0) {
            configuration.setListenPort(port);
        }
        configuration.setWorkerThreads(NumberUtils.toInt(element.getAttribute("worker-threads"), 10));
        configuration.setSelectorThreads(NumberUtils.toInt(element.getAttribute("selector-threads"), 5));
        configuration.setChannelMaxIdleTimeSeconds(NumberUtils.toInt(element.getAttribute("channel-maxidletime-seconds"), 120));
        configuration.setSocketSndBufSize(NumberUtils.toInt(element.getAttribute("socket-sndbuf-size"), 65535));
        configuration.setSocketRcvBufSize(NumberUtils.toInt(element.getAttribute("socket-rcvbuf-size"), 65535));
        configuration.setPooledByteBufAllocatorEnable(BooleanUtils.toBoolean(StringUtils.defaultString(element.getAttribute("pooled-bytebuf-allocator-enable"), "true")));
        configuration.setDefaultWorkerProcessorThreads(NumberUtils.toInt(element.getAttribute("default-worker-processor-threads"), 10));
        configuration.setDefaultExecutorThreads(NumberUtils.toInt(element.getAttribute("default-executor-threads"), 8));
        configuration.setUseEpollNativeSelector(BooleanUtils.toBoolean(StringUtils.defaultString(element.getAttribute("use-epoll-native-selector"), "false")));
        builder.addPropertyValue("configuration", configuration);
        
        List<Element> registryElements = DomUtils.getChildElementsByTagName(element, "registry");
        Element registryElement = registryElements.get(0);
        URI uri = URI.valueOf(registryElement.getAttribute("address"));
        uri = uri.addParameters(ParametersParser.parse(registryElement));
        builder.addPropertyValue("registry", new LnkRegistry(uri));
        
        String serviceObjectFinderId = "defaultServiceObjectFinder";
        BeanRegister.register(serviceObjectFinderId, DefaultServiceObjectFinder.class, element, parserContext);
        builder.addPropertyValue("serviceObjectFinder", new RuntimeBeanReference(serviceObjectFinderId));
        
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
            switch (trackType) {
                case "logger":
                    tracker = new LogTracker();
                    break;
                case "lnk":
                    break;
                case "cat":
                    break;
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
            serviceGroup.setServiceGroupWorkerProcessorThreads(NumberUtils.toInt(serviceGroupElement.getAttribute("worker-threads"), 10));
            serviceGroups.add(serviceGroup);
        }
        builder.addPropertyValue("serviceGroups", serviceGroups);
        log.info("parse LnkServer bean success.");
    }
}
