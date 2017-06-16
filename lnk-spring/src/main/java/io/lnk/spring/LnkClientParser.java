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

import io.lnk.api.URI;
import io.lnk.api.cluster.LoadBalance;
import io.lnk.cluster.ConsistencyHashLoadBalance;
import io.lnk.cluster.PriorityLocalLoadBalance;
import io.lnk.cluster.RandomLoadBalance;
import io.lnk.cluster.RoundRobinLoadBalance;
import io.lnk.core.caller.LnkRemoteObjectFactory;
import io.lnk.flow.SemaphoreFlowController;
import io.lnk.lookup.LnkRegistry;
import io.lnk.protocol.LnkProtocolFactorySelector;
import io.lnk.remoting.ClientConfiguration;
import io.lnk.remoting.RemotingProvider;
import io.lnk.spring.core.SpringLnkInvoker;
import io.lnk.spring.utils.BeanRegister;
import io.lnk.spring.utils.ParametersParser;
import io.lnk.spring.utils.BeanRegister.BeanDefinitionCallback;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月23日 下午3:56:52
 */
public class LnkClientParser extends AbstractSingleBeanDefinitionParser {
    private static final Logger log = LoggerFactory.getLogger(LnkClientParser.class.getSimpleName());

    @Override
    protected Class<?> getBeanClass(Element element) {
        return SpringLnkInvoker.class;
    }

    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
        final LnkProtocolFactorySelector protocolFactorySelector = new LnkProtocolFactorySelector();
        builder.addPropertyValue("protocolFactorySelector", protocolFactorySelector);

        List<Element> lookupElements = DomUtils.getChildElementsByTagName(element, "lookup");
        Element lookupElement = lookupElements.get(0);
        URI uri = URI.valueOf(lookupElement.getAttribute("address"));
        uri = uri.addParameters(ParametersParser.parse(lookupElement));
        builder.addPropertyValue("registry", new LnkRegistry(uri));

        String remoteObjectFactoryId = "defaultInvokerRemoteObjectFactory";
        BeanRegister.register(remoteObjectFactoryId, LnkRemoteObjectFactory.class, element, parserContext, new BeanDefinitionCallback() {
            public void doInRegister(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("invoker", new RuntimeBeanReference(LnkClientParser.this.resolveId(element)));
                beanDefinition.getPropertyValues().addPropertyValue("protocolFactorySelector", protocolFactorySelector);
            }
        });
        builder.addPropertyValue("remoteObjectFactory", new RuntimeBeanReference(remoteObjectFactoryId));

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setProvider(RemotingProvider.valueOfProvider(element.getAttribute("provider")));
        configuration.setWorkerThreads(NumberUtils.toInt(element.getAttribute("worker-threads"), 4));
        configuration.setConnectTimeoutMillis(NumberUtils.toInt(element.getAttribute("connect-timeout-millis"), 3000));
        configuration.setChannelMaxIdleTimeSeconds(NumberUtils.toInt(element.getAttribute("channel-maxidletime-seconds"), 120));
        configuration.setSocketSndBufSize(NumberUtils.toInt(element.getAttribute("socket-sndbuf-size"), 65535));
        configuration.setSocketRcvBufSize(NumberUtils.toInt(element.getAttribute("socket-rcvbuf-size"), 65535));
        configuration.setDefaultExecutorThreads(NumberUtils.toInt(element.getAttribute("default-executor-threads"), 4));
        builder.addPropertyValue("configuration", configuration);
        List<Element> loadBalanceElements = DomUtils.getChildElementsByTagName(element, "load-balance");
        Element loadBalanceElement = loadBalanceElements.get(0);
        String loadBalanceType = StringUtils.defaultString(loadBalanceElement.getAttribute("type"));
        LoadBalance loadBalance = null;
        switch (loadBalanceType) {
            case "hash":
                loadBalance = new ConsistencyHashLoadBalance();
                break;
            case "random":
                loadBalance = new RandomLoadBalance();
                break;
            case "roundrobin":
                loadBalance = new RoundRobinLoadBalance();
                break;
            case "local":
                loadBalance = new PriorityLocalLoadBalance();
                break;
            default:
                loadBalance = new ConsistencyHashLoadBalance();
                break;
        }
        ParametersParser.wiredParameters(loadBalanceElement, loadBalance);
        builder.addPropertyValue("loadBalance", loadBalance);

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
