package io.lnk.spring;

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
import org.w3c.dom.Element;

import io.lnk.api.app.Application;
import io.lnk.config.ctx.config.PlaceholderConfiguration;
import io.lnk.config.ctx.ns.NsRegistryImpl;
import io.lnk.spring.core.LnkApplication;
import io.lnk.spring.utils.LnkComponentParameterUtils;
import io.lnk.spring.utils.LnkComponentUtils;
import io.lnk.spring.utils.LnkComponentUtils.ComponentCallback;

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
        final String app = element.getAttribute("app");
        final String nsHome = element.getAttribute("ns-home");
        application.setApp(app);
        application.setType(element.getAttribute("type"));
        application.setParameters(LnkComponentParameterUtils.parse(element));
        builder.addPropertyValue("application", application);
        LnkComponentUtils.parse("configuration", PlaceholderConfiguration.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("systemId", app);
            }
        });
        String nsRegistryId = "nsRegistry";
        LnkComponentUtils.parse(nsRegistryId, NsRegistryImpl.class, element, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("nsHome", nsHome);
            }
        });
        builder.addPropertyValue("nsRegistry", new RuntimeBeanReference(nsRegistryId));
        log.info("parse LnkApplication bean success.");
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        return LnkApplication.LNK_APPLICATION_NAME;
    }
}
