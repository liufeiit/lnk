package io.lnk.spring.utils;

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
public class LnkComponentUtils {
    
    public static void parse(String id, Class<?> beanType, Element rootElement, ParserContext parserContext) {
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
        } catch (Throwable e) {
            parserContext.getReaderContext().error(e.getMessage(), rootElement);
        }
    }
    
    public static void parse(String id, Class<?> beanType, Element rootElement, ParserContext parserContext, ComponentCallback callback) {
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
            callback.onParse(beanDefinition);
            BeanDefinitionHolder beanholder = new BeanDefinitionHolder(beanDefinition, id);
            BeanDefinitionReaderUtils.registerBeanDefinition(beanholder, parserContext.getRegistry());
            BeanComponentDefinition componentDefinition = new BeanComponentDefinition(beanholder);
            parserContext.registerComponent(componentDefinition);
        } catch (Throwable e) {
            parserContext.getReaderContext().error(e.getMessage(), rootElement);
        }
    }
    
    public static interface ComponentCallback {
        void onParse(RootBeanDefinition beanDefinition);
    }
}
