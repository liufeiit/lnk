package io.lnk.spring.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月6日 下午4:37:35
 */
public class BeanRegister {
    
    private static final Logger log = LoggerFactory.getLogger(BeanRegister.class.getSimpleName());

    public static void register(String id, Class<?> beanType, Element rootElement, ParserContext parserContext) {
        try {
            Object eleSource = parserContext.extractSource(rootElement);
            RootBeanDefinition beanDefinition = new RootBeanDefinition();
            beanDefinition.setSource(eleSource);
            beanDefinition.setRole(BeanDefinition.ROLE_APPLICATION);
            beanDefinition.setBeanClass(beanType);
            beanDefinition.setLazyInit(false);
            beanDefinition.setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_NONE);
            beanDefinition.setAutowireCandidate(true);
            beanDefinition.setAutowireMode(RootBeanDefinition.AUTOWIRE_BY_NAME);
            BeanDefinitionHolder beanholder = new BeanDefinitionHolder(beanDefinition, id);
            BeanDefinitionReaderUtils.registerBeanDefinition(beanholder, parserContext.getRegistry());
            BeanComponentDefinition componentDefinition = new BeanComponentDefinition(beanholder);
            parserContext.registerComponent(componentDefinition);
            log.info("BeanRegister#register BeanDefinition : {}", id);
        } catch (Throwable e) {
            log.error("BeanRegister#register BeanDefinition " + id + " Error.", e);
            parserContext.getReaderContext().error(e.getMessage(), rootElement);
        }
    }
    
    public static void register(String id, Class<?> beanType, Element rootElement, ParserContext parserContext, BeanDefinitionCallback callback) {
        try {
            Object eleSource = parserContext.extractSource(rootElement);
            RootBeanDefinition beanDefinition = new RootBeanDefinition();
            beanDefinition.setSource(eleSource);
            beanDefinition.setRole(BeanDefinition.ROLE_APPLICATION);
            beanDefinition.setBeanClass(beanType);
            beanDefinition.setLazyInit(false);
            beanDefinition.setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_NONE);
            beanDefinition.setAutowireCandidate(true);
            beanDefinition.setAutowireMode(RootBeanDefinition.AUTOWIRE_BY_NAME);
            callback.doInRegister(beanDefinition);
            BeanDefinitionHolder beanholder = new BeanDefinitionHolder(beanDefinition, id);
            BeanDefinitionReaderUtils.registerBeanDefinition(beanholder, parserContext.getRegistry());
            BeanComponentDefinition componentDefinition = new BeanComponentDefinition(beanholder);
            parserContext.registerComponent(componentDefinition);
            log.info("BeanRegister#register BeanDefinition : {}", id);
        } catch (Throwable e) {
            log.error("BeanRegister#register BeanDefinition " + id + " Error.", e);
            parserContext.getReaderContext().error(e.getMessage(), rootElement);
        }
    }
    
    public static interface BeanDefinitionCallback {
        void doInRegister(RootBeanDefinition beanDefinition);
    }
}
