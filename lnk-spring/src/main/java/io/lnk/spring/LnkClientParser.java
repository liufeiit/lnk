package io.lnk.spring;

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

import io.lnk.api.ClientConfiguration;
import io.lnk.api.URI;
import io.lnk.api.cluster.LoadBalance;
import io.lnk.cluster.ConsistencyHashLoadBalance;
import io.lnk.cluster.PriorityLocalLoadBalance;
import io.lnk.cluster.RandomLoadBalance;
import io.lnk.cluster.RoundRobinLoadBalance;
import io.lnk.core.caller.LnkBrokerCaller;
import io.lnk.core.caller.LnkRemoteObjectFactory;
import io.lnk.flow.SemaphoreFlowController;
import io.lnk.lookup.LnkRegistry;
import io.lnk.protocol.LnkProtocolFactorySelector;
import io.lnk.protocol.broker.LnkBrokerProtocolFactorySelector;
import io.lnk.protocol.object.LnkObjectProtocolFactory;
import io.lnk.spring.core.SpringLnkInvoker;
import io.lnk.spring.utils.LnkComponentUtils;
import io.lnk.spring.utils.LnkComponentUtils.ComponentCallback;
import io.lnk.spring.utils.LnkComponentParameterUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月23日 下午3:56:52
 */
public class LnkClientParser extends AbstractSingleBeanDefinitionParser {
    private static final Logger log = LoggerFactory.getLogger(LnkClientParser.class.getSimpleName());
    private static final String DEFAULT_EXECUTOR_THREADS_ATTR = "default-executor-threads";
    private static final String SOCKET_RCVBUF_SIZE_ATTR = "socket-rcvbuf-size";
    private static final String SOCKET_SNDBUF_SIZE_ATTR = "socket-sndbuf-size";
    private static final String CHANNEL_MAXIDLETIME_SECONDS_ATTR = "channel-maxidletime-seconds";
    private static final String CONNECT_TIMEOUT_MILLIS_ATTR = "connect-timeout-millis";
    private static final String WORKER_THREADS_ATTR = "worker-threads";
    private static final String PROVIDER_ATTR = "provider";

    @Override
    protected Class<?> getBeanClass(Element element) {
        return SpringLnkInvoker.class;
    }

    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        final String invokerId = this.resolveId(element);
        AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
        
        final String protocolFactorySelectorId = invokerId + ".ProtocolFactorySelector";
        final String brokerCallerId = invokerId + ".BrokerCaller";
        final String remoteObjectFactoryId = invokerId + ".RemoteObjectFactory";
        final String objectProtocolFactoryId = invokerId + ".ObjectProtocolFactory";
        final String brokerProtocolFactorySelectorId = invokerId + ".BrokerProtocolFactorySelector";
        
        LnkComponentUtils.parse(objectProtocolFactoryId, LnkObjectProtocolFactory.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("remoteObjectFactory", new RuntimeBeanReference(remoteObjectFactoryId));
            }
        });
        
        LnkComponentUtils.parse(protocolFactorySelectorId, LnkProtocolFactorySelector.class, element, parserContext);
        builder.addPropertyValue("protocolFactorySelector", new RuntimeBeanReference(protocolFactorySelectorId));
        
        LnkComponentUtils.parse(brokerProtocolFactorySelectorId, LnkBrokerProtocolFactorySelector.class, element, parserContext);
        
        LnkComponentUtils.parse(brokerCallerId, LnkBrokerCaller.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("invoker", new RuntimeBeanReference(invokerId));
                beanDefinition.getPropertyValues().addPropertyValue("brokerProtocolFactorySelector", new RuntimeBeanReference(brokerProtocolFactorySelectorId));
                beanDefinition.getPropertyValues().addPropertyValue("protocolFactorySelector", new RuntimeBeanReference(protocolFactorySelectorId));
                beanDefinition.getPropertyValues().addPropertyValue("objectProtocolFactory", new RuntimeBeanReference(objectProtocolFactoryId));
            }
        });
        builder.addPropertyValue("brokerCaller", new RuntimeBeanReference(brokerCallerId));
        
        List<Element> lookupElements = DomUtils.getChildElementsByTagName(element, "lookup");
        Element lookupElement = lookupElements.get(0);
        URI uri = URI.valueOf(lookupElement.getAttribute("address"));
        uri = uri.addParameters(LnkComponentParameterUtils.parse(lookupElement));
        builder.addPropertyValue("registry", new LnkRegistry(uri));

        LnkComponentUtils.parse(remoteObjectFactoryId, LnkRemoteObjectFactory.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("invoker", new RuntimeBeanReference(invokerId));
                beanDefinition.getPropertyValues().addPropertyValue("protocolFactorySelector", new RuntimeBeanReference(protocolFactorySelectorId));
                beanDefinition.getPropertyValues().addPropertyValue("objectProtocolFactory", new RuntimeBeanReference(objectProtocolFactoryId));
            }
        });
        builder.addPropertyValue("remoteObjectFactory", new RuntimeBeanReference(remoteObjectFactoryId));

        String provider = element.getAttribute(PROVIDER_ATTR);
        String workerThreads = element.getAttribute(WORKER_THREADS_ATTR);
        String connectTimeoutMillis = element.getAttribute(CONNECT_TIMEOUT_MILLIS_ATTR);
        String channelMaxidletimeSeconds = element.getAttribute(CHANNEL_MAXIDLETIME_SECONDS_ATTR);
        String socketSndbufSize = element.getAttribute(SOCKET_SNDBUF_SIZE_ATTR);
        String socketRcvbufSize = element.getAttribute(SOCKET_RCVBUF_SIZE_ATTR);
        String defaultExecutorThreads = element.getAttribute(DEFAULT_EXECUTOR_THREADS_ATTR);
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setProvider(provider);
        configuration.setWorkerThreads(NumberUtils.toInt(workerThreads, 4));
        configuration.setConnectTimeoutMillis(NumberUtils.toInt(connectTimeoutMillis, 3000));
        configuration.setChannelMaxIdleTimeSeconds(NumberUtils.toInt(channelMaxidletimeSeconds, 120));
        configuration.setSocketSndBufSize(NumberUtils.toInt(socketSndbufSize, 65535));
        configuration.setSocketRcvBufSize(NumberUtils.toInt(socketRcvbufSize, 65535));
        configuration.setDefaultExecutorThreads(NumberUtils.toInt(defaultExecutorThreads, 4));
        log.info("LnkInvoker[{}] configuration : {}", invokerId, configuration);
        builder.addPropertyValue("configuration", configuration);

        List<Element> loadBalanceElements = DomUtils.getChildElementsByTagName(element, "load-balance");
        Element loadBalanceElement = loadBalanceElements.get(0);
        String loadBalanceType = StringUtils.defaultString(loadBalanceElement.getAttribute("type"));
        LoadBalance loadBalance = null;
        if (StringUtils.equals(loadBalanceType, "hash")) {
            loadBalance = new ConsistencyHashLoadBalance();
        } else if (StringUtils.equals(loadBalanceType, "random")) {
            loadBalance = new RandomLoadBalance();
        } else if (StringUtils.equals(loadBalanceType, "roundrobin")) {
            loadBalance = new RoundRobinLoadBalance();
        } else if (StringUtils.equals(loadBalanceType, "local")) {
            loadBalance = new PriorityLocalLoadBalance();
        } else {
            loadBalance = new ConsistencyHashLoadBalance();
        }
        LnkComponentParameterUtils.wiredParameters(loadBalanceElement, loadBalance);
        builder.addPropertyValue("loadBalance", loadBalance);

        List<Element> flowControlElements = DomUtils.getChildElementsByTagName(element, "flow-control");
        if (CollectionUtils.isNotEmpty(flowControlElements)) {
            Element flowControlElement = flowControlElements.get(0);
            String permitsString = StringUtils.defaultString(flowControlElement.getAttribute("permits"));
            int permits = NumberUtils.toInt(permitsString);
            if (StringUtils.isNotBlank(permitsString) && permits > 0) {
                SemaphoreFlowController flowController = new SemaphoreFlowController(permits);
                LnkComponentParameterUtils.wiredParameters(flowControlElement, flowController);
                builder.addPropertyValue("flowController", flowController);
            }
        }
        log.info("parse LnkInvoker bean success.");
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        return this.resolveId(element);
    }

    private String resolveId(Element element) {
        return element.getAttribute(ID_ATTRIBUTE);
    }
}
