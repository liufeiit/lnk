package io.lnk.framework.dispatcher.spi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月2日 下午3:17:02
 */
public class InvokersDispatcherAgentFactory implements FactoryBean<Object>, InitializingBean {

    protected final static Logger log = LoggerFactory.getLogger(InvokersDispatcherAgentFactory.class.getSimpleName());

    private Class<?> dispatcherType;

    private InvocationHandler invocationHandler;

    private InvokersDispatcher invokersDispatcher;

    @Override
    public void afterPropertiesSet() throws Exception {
        invocationHandler = new InvokersDispatcherAgent(invokersDispatcher, dispatcherType);
        log.info("Build InvokersDispatcherAgent agentType : {}", dispatcherType);
    }

    @Override
    public Object getObject() throws Exception {
        return Proxy.newProxyInstance(dispatcherType.getClassLoader(), new Class[] {dispatcherType}, invocationHandler);
    }

    @Override
    public Class<?> getObjectType() {
        return dispatcherType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setDispatcherType(Class<?> dispatcherType) {
        this.dispatcherType = dispatcherType;
    }

    public void setInvokersDispatcher(InvokersDispatcher invokersDispatcher) {
        this.invokersDispatcher = invokersDispatcher;
    }
}
