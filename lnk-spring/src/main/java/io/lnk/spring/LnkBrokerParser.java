package io.lnk.spring;

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
import org.w3c.dom.Element;

import io.lnk.api.ServerConfiguration;
import io.lnk.port.DefaultServerPortAllocator;
import io.lnk.spring.broker.DefaultBrokerServer;
import io.lnk.spring.utils.LnkComponentUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月23日 下午3:56:52
 */
public class LnkBrokerParser extends AbstractSingleBeanDefinitionParser {
    private static final Logger log = LoggerFactory.getLogger(LnkBrokerParser.class.getSimpleName());
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
        return DefaultBrokerServer.class;
    }

    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
        String serverId = this.resolveId(element);
        String serverPortAllocatorId = "defaultBrokerServerPortAllocator";
        
        LnkComponentUtils.parse(serverPortAllocatorId, DefaultServerPortAllocator.class, element, parserContext);
        builder.addPropertyValue("serverPortAllocator", new RuntimeBeanReference(serverPortAllocatorId));

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
        configuration.setProvider(provider);
        configuration.setWorkerThreads(NumberUtils.toInt(workerThreads, 10));
        configuration.setSelectorThreads(NumberUtils.toInt(selectorThreads, 5));
        configuration.setChannelMaxIdleTimeSeconds(NumberUtils.toInt(channelMaxidletimeSeconds, 120));
        configuration.setSocketSndBufSize(NumberUtils.toInt(socketSndbufSize, 65535));
        configuration.setSocketRcvBufSize(NumberUtils.toInt(socketRcvbufSize, 65535));
        configuration.setPooledByteBufAllocatorEnable(BooleanUtils.toBoolean(StringUtils.defaultString(pooledBytebufAllocatorEnable, "true")));
        configuration.setDefaultWorkerProcessorThreads(NumberUtils.toInt(defaultWorkerProcessorThreads, 10));
        configuration.setDefaultExecutorThreads(NumberUtils.toInt(defaultExecutorThreads, 8));
        configuration.setUseEpollNativeSelector(BooleanUtils.toBoolean(StringUtils.defaultString(useEpollNativeSelector, "false")));
        log.info("BrokerServer[{}] configuration : {}", serverId, configuration);
        builder.addPropertyValue("configuration", configuration);
        log.info("parse BrokerServer bean success.");
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        return this.resolveId(element);
    }

    private String resolveId(Element element) {
        return element.getAttribute(ID_ATTRIBUTE);
    }
}
