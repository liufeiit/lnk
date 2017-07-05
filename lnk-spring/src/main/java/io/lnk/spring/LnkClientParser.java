package io.lnk.spring;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import io.lnk.cluster.NestedLoadBalance;
import io.lnk.core.caller.LnkRemoteObjectFactory;
import io.lnk.flow.SemaphoreFlowController;
import io.lnk.lookup.ZooKeeperRegistry;
import io.lnk.lookup.zookeeper.DefaultZooKeeperService;
import io.lnk.protocol.LnkProtocolFactorySelector;
import io.lnk.protocol.object.LnkObjectProtocolFactory;
import io.lnk.spring.core.SpringLnkInvoker;
import io.lnk.spring.utils.LnkComponentParameterUtils;
import io.lnk.spring.utils.LnkComponentUtils;
import io.lnk.spring.utils.LnkComponentUtils.ComponentCallback;

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
    private static final String PROTOCOL_ATTR = "protocol";

    @Override
    protected Class<?> getBeanClass(Element element) {
        return SpringLnkInvoker.class;
    }

    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        final String invokerId = this.resolveId(element);
        AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
        final String protocolFactorySelectorId = invokerId + ".ProtocolFactorySelector";
        final String remoteObjectFactoryId = invokerId + ".RemoteObjectFactory";
        final String objectProtocolFactoryId = invokerId + ".ObjectProtocolFactory";
        final String clientConfigurationId = invokerId + "ClientConfiguration";

        LnkComponentUtils.parse(objectProtocolFactoryId, LnkObjectProtocolFactory.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("remoteObjectFactory", new RuntimeBeanReference(remoteObjectFactoryId));
            }
        });

        LnkComponentUtils.parse(protocolFactorySelectorId, LnkProtocolFactorySelector.class, element, parserContext);
        builder.addPropertyValue("protocolFactorySelector", new RuntimeBeanReference(protocolFactorySelectorId));

        List<Element> lookupElements = DomUtils.getChildElementsByTagName(element, "lookup");
        final Element lookupElement = lookupElements.get(0);
        final String zooKeeperServiceId = "zooKeeperService";
        LnkComponentUtils.parse(zooKeeperServiceId, DefaultZooKeeperService.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("zookeeperUri", lookupElement.getAttribute("address"));
            }
        });
        String lookupId = "lnkLookup";
        LnkComponentUtils.parse(lookupId, ZooKeeperRegistry.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("zooKeeperService", new RuntimeBeanReference(zooKeeperServiceId));
                beanDefinition.getPropertyValues().addPropertyValues(LnkComponentParameterUtils.parse(lookupElement));
            }
        });
        builder.addPropertyValue("registry", new RuntimeBeanReference(lookupId));

        LnkComponentUtils.parse(remoteObjectFactoryId, LnkRemoteObjectFactory.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("invoker", new RuntimeBeanReference(invokerId));
                beanDefinition.getPropertyValues().addPropertyValue("protocolFactorySelector", new RuntimeBeanReference(protocolFactorySelectorId));
                beanDefinition.getPropertyValues().addPropertyValue("objectProtocolFactory", new RuntimeBeanReference(objectProtocolFactoryId));
            }
        });
        builder.addPropertyValue("remoteObjectFactory", new RuntimeBeanReference(remoteObjectFactoryId));

        LnkComponentUtils.parse(clientConfigurationId, ClientConfiguration.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                String protocol = element.getAttribute(PROTOCOL_ATTR);
                String workerThreads = element.getAttribute(WORKER_THREADS_ATTR);
                String connectTimeoutMillis = element.getAttribute(CONNECT_TIMEOUT_MILLIS_ATTR);
                String channelMaxIdleTimeSeconds = element.getAttribute(CHANNEL_MAXIDLETIME_SECONDS_ATTR);
                String socketSndBufSize = element.getAttribute(SOCKET_SNDBUF_SIZE_ATTR);
                String socketRcvBufSize = element.getAttribute(SOCKET_RCVBUF_SIZE_ATTR);
                String defaultExecutorThreads = element.getAttribute(DEFAULT_EXECUTOR_THREADS_ATTR);
                beanDefinition.getPropertyValues().addPropertyValue("protocol", protocol);
                beanDefinition.getPropertyValues().addPropertyValue("workerThreads", workerThreads);
                beanDefinition.getPropertyValues().addPropertyValue("connectTimeoutMillis", connectTimeoutMillis);
                beanDefinition.getPropertyValues().addPropertyValue("channelMaxIdleTimeSeconds", channelMaxIdleTimeSeconds);
                beanDefinition.getPropertyValues().addPropertyValue("socketSndBufSize", socketSndBufSize);
                beanDefinition.getPropertyValues().addPropertyValue("socketRcvBufSize", socketRcvBufSize);
                beanDefinition.getPropertyValues().addPropertyValue("defaultExecutorThreads", defaultExecutorThreads);
            }
        });
        log.info("LnkInvoker[{}] configuration : {}", invokerId, clientConfigurationId);
        builder.addPropertyValue("configuration", new RuntimeBeanReference(clientConfigurationId));

        List<Element> loadBalanceElements = DomUtils.getChildElementsByTagName(element, "load-balance");
        final Element loadBalanceElement = loadBalanceElements.get(0);
        String loadBalanceId = "nestedLoadBalance";
        LnkComponentUtils.parse(loadBalanceId, NestedLoadBalance.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, loadBalanceElement.getAttribute("type"));
                beanDefinition.getPropertyValues().addPropertyValues(LnkComponentParameterUtils.parse(loadBalanceElement));
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
                        beanDefinition.getPropertyValues().addPropertyValues(LnkComponentParameterUtils.parse(flowControlElement));
                    }
                });
                builder.addPropertyValue("flowController", new RuntimeBeanReference(flowControllerId));
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
