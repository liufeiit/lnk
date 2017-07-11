package io.lnk.framework.dispatcher.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月2日 下午2:20:19
 */
public class InvokersDispatcherHandler implements InvocationHandler {
    private final Class<?> dispatcherType;
    private final InvokersDispatcher invokersDispatcher;

    public InvokersDispatcherHandler(InvokersDispatcher invokersDispatcher, Class<?> dispatcherType) {
        super();
        this.invokersDispatcher = invokersDispatcher;
        this.dispatcherType = dispatcherType;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String _methodName = method.getName();
        String methodName = method.getDeclaringClass().getName() + "." + _methodName;
        switch (_methodName) {
            case "getClass":
                return dispatcherType;
            case "hashCode":
                return method.hashCode();
            case "equals":
                return false;
            case "toString":
                return "Proxy$" + dispatcherType.getName();
            default:
                break;
        }
        if (!method.isAnnotationPresent(InvokerMethod.class)) {
            throw new IllegalAccessException(methodName + " must Annotation by @InvokerMethod.");
        }
        Annotation[][] parametersAnnotations = method.getParameterAnnotations();
        if (parametersAnnotations == null) {
            throw new IllegalAccessException(methodName + " parameters must Annotation by InvokerCode or InvokerRequest.");
        }
        return invokersDispatcher.invoke((InvokerTypeCode) args[0], args[1]);
    }
}
