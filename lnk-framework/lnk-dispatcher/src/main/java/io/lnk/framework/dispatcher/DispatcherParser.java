package io.lnk.framework.dispatcher;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanExpressionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import io.lnk.framework.dispatcher.spi.InvokerTypeCode;
import io.lnk.framework.dispatcher.spi.InvokersDispatcher;
import io.lnk.framework.dispatcher.spi.InvokersDispatcherAgentFactory;
import io.lnk.framework.utils.FieldRetriever;
import io.lnk.framework.utils.LnkComponentUtils;
import io.lnk.framework.utils.LnkComponentUtils.ComponentCallback;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月1日 下午4:39:48
 */
public class DispatcherParser implements BeanDefinitionParser {

    protected static final Logger log = LoggerFactory.getLogger(DispatcherParser.class.getSimpleName());

    @Override
    public BeanDefinition parse(Element rootElement, ParserContext parserContext) {
        List<Element> dispatcherInvokers = DomUtils.getChildElementsByTagName(rootElement, "invokers");
        if (CollectionUtils.isEmpty(dispatcherInvokers) == false) {
            for (Element dispatcherInvoker : dispatcherInvokers) {
                final String dispatcherInvokerId = dispatcherInvoker.getAttribute("id");
                final String dispatcherInvokerType = dispatcherInvoker.getAttribute("dispatcher-invoker");
                List<Element> invokers = DomUtils.getChildElementsByTagName(dispatcherInvoker, "invoker");
                if (CollectionUtils.isEmpty(invokers) == false) {
                    final ManagedMap<String, Object> invokersManagedMap = new ManagedMap<String, Object>();
                    for (Element invoker : invokers) {
                        invokersManagedMap.put(this.parseInvokerTypeCode(invoker.getAttribute("invoker-code")), new RuntimeBeanReference(invoker.getAttribute("invoker-handler")));
                    }
                    final String invokersDispatcherId = LnkComponentUtils.parse(InvokersDispatcher.class, rootElement, parserContext, new ComponentCallback() {
                        public void onParse(RootBeanDefinition beanDefinition) {
                            beanDefinition.getPropertyValues().addPropertyValue("invokers", invokersManagedMap);
                        }
                    });
                    LnkComponentUtils.parse(dispatcherInvokerId, InvokersDispatcherAgentFactory.class, rootElement, parserContext, new ComponentCallback() {
                        public void onParse(RootBeanDefinition beanDefinition) {
                            beanDefinition.getPropertyValues().addPropertyValue("invokersDispatcher", new RuntimeBeanReference(invokersDispatcherId));
                            beanDefinition.getPropertyValues().addPropertyValue("dispatcherType", dispatcherInvokerType);
                        }
                    });
                }
            }
        }
        return null;
    }

    private String parseInvokerTypeCode(String invokerCode) {
        try {
            InvokerTypeCode invokerTypeCode = FieldRetriever.getObject(invokerCode, InvokerTypeCode.class);
            return invokerTypeCode.invokerCode();
        } catch (Throwable e) {
            log.error("DispatcherParser#parseInvokerTypeCode Invoker InvokerTypeCode Attribute 'invoker-code' " + invokerCode + " Error.", e);
            throw new BeanExpressionException("Invoker InvokerTypeCode Attribute 'invoker-code' " + invokerCode + " Error.", e);
        }
    }
}
