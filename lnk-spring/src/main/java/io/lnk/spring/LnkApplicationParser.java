package io.lnk.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import io.lnk.api.app.Application;
import io.lnk.spring.core.LnkApplication;
import io.lnk.spring.utils.LnkComponentParameterUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月23日 下午3:56:52
 */
public class LnkApplicationParser extends AbstractSingleBeanDefinitionParser {
    private static final Logger log = LoggerFactory.getLogger(LnkApplicationParser.class.getSimpleName());
    
    @Override
    protected Class<?> getBeanClass(Element element) {
        return LnkApplication.class;
    }
    
    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
        final Application application = new Application();
        application.setApp(element.getAttribute("app"));
        application.setType(element.getAttribute("type"));
        application.setParameters(LnkComponentParameterUtils.parse(element));
        builder.addPropertyValue("application", application);
        log.info("parse LnkApplication bean success.");
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        return LnkApplication.LNK_APPLICATION_NAME;
    }
}
