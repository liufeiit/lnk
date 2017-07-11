package io.lnk.framework.dispatcher.spi;

import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月2日 下午3:17:02
 */
public class InvokersDispatcherHandlerFactory implements BeanClassLoaderAware, FactoryBean<Object> {
    private final static Logger log = LoggerFactory.getLogger(InvokersDispatcherHandlerFactory.class.getSimpleName());
    private Class<?> dispatcherType;
    private InvokersDispatcher invokersDispatcher;
    private ClassLoader classLoader;

    @Override
    public Object getObject() throws Exception {
        log.info("build dispatcherType : {}", dispatcherType);
        return Proxy.newProxyInstance(classLoader, new Class[] {dispatcherType}, new InvokersDispatcherHandler(invokersDispatcher, dispatcherType));
    }

    @Override
    public Class<?> getObjectType() {
        return dispatcherType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setDispatcherType(Class<?> dispatcherType) {
        this.dispatcherType = dispatcherType;
    }

    public void setInvokersDispatcher(InvokersDispatcher invokersDispatcher) {
        this.invokersDispatcher = invokersDispatcher;
    }
}
