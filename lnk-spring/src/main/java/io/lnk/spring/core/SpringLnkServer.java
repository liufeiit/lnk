package io.lnk.spring.core;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
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

import io.lnk.api.ServiceVersion;
import io.lnk.api.annotation.LnkService;
import io.lnk.api.annotation.LnkServiceVersion;
import io.lnk.core.lnk.DefaultLnkServer;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午2:16:27
 */
public class SpringLnkServer extends DefaultLnkServer implements BeanFactoryAware, ApplicationListener<ApplicationEvent>, BeanPostProcessor, PriorityOrdered, SmartLifecycle, InitializingBean, DisposableBean {
    private BeanFactory beanFactory;
    private Set<Object> exportServices = new HashSet<Object>();

    @Override
    public void afterPropertiesSet() throws Exception {
        LnkApplication lnkApplication = this.beanFactory.getBean(LnkApplication.LNK_APPLICATION_NAME, LnkApplication.class);
        super.setApplication(lnkApplication.getApplication());
        super.start();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanType = bean.getClass();
        String version = ServiceVersion.DEFAULT_VERSION;
        if (beanType.isAnnotationPresent(LnkServiceVersion.class)) {
            LnkServiceVersion lnkServiceVersion = beanType.getAnnotation(LnkServiceVersion.class);
            version = lnkServiceVersion.version();
        }
        this.serviceRegistry(beanType, bean, version);
        return bean;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextClosedEvent) {
            super.shutdown();
        }
    }
    
    private void serviceUnregistry(Class<?> beanType, Object bean, String version) {
        Class<?>[] beanInterfaces = beanType.getInterfaces();
        if (ArrayUtils.isNotEmpty(beanInterfaces)) {
            for (Class<?> beanInterface : beanInterfaces) {
                if (beanInterface.isInterface()) {
                    if (beanInterface.isAnnotationPresent(LnkService.class)) {
                        LnkService lnkService = beanInterface.getAnnotation(LnkService.class);
                        super.unregistry(lnkService.group(), beanInterface.getName(), version, lnkService.protocol());
                    }
                } else {
                    this.serviceUnregistry(beanInterface, bean, version);
                }
            }
        }
    }

    private void serviceRegistry(Class<?> beanType, Object bean, String version) {
        Class<?>[] beanInterfaces = beanType.getInterfaces();
        if (ArrayUtils.isNotEmpty(beanInterfaces)) {
            for (Class<?> beanInterface : beanInterfaces) {
                if (beanInterface.isInterface()) {
                    if (beanInterface.isAnnotationPresent(LnkService.class)) {
                        this.exportServices.add(bean);
                        LnkService lnkService = beanInterface.getAnnotation(LnkService.class);
                        super.registry(lnkService.group(), beanInterface.getName(), version, lnkService.protocol(), bean);
                    }
                } else {
                    this.serviceRegistry(beanInterface, bean, version);
                }
            }
        }
    }

    @Override
    public int getPhase() {
        return this.getOrder();
    }

    @Override
    public void stop() {}

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

    protected void shutdown0() throws Throwable {
        if (CollectionUtils.isEmpty(this.exportServices)) {
            return;
        }
        for (Object exportService : this.exportServices) {
            Class<?> beanType = exportService.getClass();
            String version = ServiceVersion.DEFAULT_VERSION;
            if (beanType.isAnnotationPresent(LnkServiceVersion.class)) {
                LnkServiceVersion lnkServiceVersion = beanType.getAnnotation(LnkServiceVersion.class);
                version = lnkServiceVersion.version();
            }
            this.serviceUnregistry(beanType, exportService, version);
        }
        this.exportServices.clear();
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
