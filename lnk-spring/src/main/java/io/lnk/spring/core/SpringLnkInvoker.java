package io.lnk.spring.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

import io.lnk.api.RemoteObjectFactoryAware;
import io.lnk.api.broker.BrokerCallerAware;
import io.lnk.core.lnk.DefaultLnkInvoker;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午2:16:51
 */
public class SpringLnkInvoker extends DefaultLnkInvoker implements BeanFactoryAware, BeanPostProcessor, ApplicationListener<ApplicationEvent>, PriorityOrdered, SmartLifecycle, InitializingBean, DisposableBean {
    private BeanFactory beanFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        LnkApplication lnkApplication = this.beanFactory.getBean(LnkApplication.LNK_APPLICATION_NAME, LnkApplication.class);
        super.setApplication(lnkApplication.getApplication());
        super.start();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RemoteObjectFactoryAware) {
            ((RemoteObjectFactoryAware) bean).setRemoteObjectFactory(super.remoteObjectFactory);
        }
        if (bean instanceof BrokerCallerAware) {
            ((BrokerCallerAware) bean).setBrokerCaller(super.brokerCaller);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), new LnkwiredFieldCallback(super.remoteObjectFactory, bean), new LnkwiredFieldFilter());
        return bean;
    }
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextClosedEvent) {
            super.shutdown();
        }
    }

    @Override
    public int getPhase() {
        return this.getOrder();
    }

    @Override
    public void stop() {
        super.shutdown();
    }

    @Override
    public boolean isRunning() {
        return super.isStarted();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        super.shutdown();
        callback.run();
    }

    @Override
    public void destroy() throws Exception {
        super.shutdown();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
