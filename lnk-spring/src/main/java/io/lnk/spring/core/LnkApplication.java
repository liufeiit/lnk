package io.lnk.spring.core;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import io.lnk.api.app.Application;
import io.lnk.api.app.ApplicationAware;
import io.lnk.config.ctx.ns.NsRegistry;
import io.lnk.config.ctx.ns.NsRegistryAware;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月12日 下午8:18:55
 */
public class LnkApplication implements BeanFactoryPostProcessor, PriorityOrdered, ApplicationListener<ApplicationEvent>, ApplicationEventPublisherAware {
    public static final String LNK_APPLICATION_NAME = "lnk.application";
    private ApplicationEventPublisher applicationEventPublisher;
    private Application application;
    private NsRegistry nsRegistry;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Map<String, ApplicationAware> applicationAwares = beanFactory.getBeansOfType(ApplicationAware.class);
        if (MapUtils.isNotEmpty(applicationAwares)) {
            for (Map.Entry<String, ApplicationAware> e : applicationAwares.entrySet()) {
                ApplicationAware applicationAware = e.getValue();
                applicationAware.setApplication(this.application);
            }
        }
        Map<String, NsRegistryAware> nsRegistryAwares = beanFactory.getBeansOfType(NsRegistryAware.class);
        if (MapUtils.isNotEmpty(nsRegistryAwares)) {
            for (Map.Entry<String, NsRegistryAware> e : nsRegistryAwares.entrySet()) {
                NsRegistryAware nsRegistryAware = e.getValue();
                nsRegistryAware.setNsRegistry(this.nsRegistry);
            }
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            this.applicationEventPublisher.publishEvent(new LnkApplicationEvent(this.application));
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Application getApplication() {
        return application;
    }
    
    public void setNsRegistry(NsRegistry nsRegistry) {
        this.nsRegistry = nsRegistry;
    }
}
