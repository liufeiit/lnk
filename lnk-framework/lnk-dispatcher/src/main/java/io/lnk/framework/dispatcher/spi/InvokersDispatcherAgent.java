package io.lnk.framework.dispatcher.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月2日 下午2:20:19
 */
public class InvokersDispatcherAgent implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(InvokersDispatcherAgent.class.getSimpleName());

    private final class ArgsIndex {
        Integer invokerCodeArgIndex;
        Integer invokerRequestArgIndex;
    }

    private final ConcurrentHashMap<String, ArgsIndex> argsIndexMappers;

    private final Class<?> agentType;

    private final InvokersDispatcher invokersDispatcher;

    public InvokersDispatcherAgent(InvokersDispatcher invokersDispatcher, Class<?> agentType) {
        super();
        this.invokersDispatcher = invokersDispatcher;
        this.agentType = agentType;
        argsIndexMappers = new ConcurrentHashMap<String, ArgsIndex>();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String _methodName = method.getName();
        String methodName = method.getDeclaringClass().getName() + "." + _methodName;
        switch (_methodName) {
            case "getClass":
                return agentType;
            case "hashCode":
                return method.hashCode();
            case "equals":
                return false;
            case "toString":
                return "Proxy$" + agentType.getName();
            default:
                break;
        }
        if (!method.isAnnotationPresent(InvokerMethod.class)) {
            throw new IllegalAccessException(methodName + " must Annotation by InvokerMethod.");
        }
        Annotation[][] parametersAnnotations = method.getParameterAnnotations();
        if (parametersAnnotations == null) {
            throw new IllegalAccessException(methodName + " parameters must Annotation by InvokerCode or InvokerRequest.");
        }
        InvokerTypeCode invokerCodeArg = null;
        Object invokerRequestArg = null;
        ArgsIndex argsIndex = argsIndexMappers.get(methodName);
        if (argsIndex != null && argsIndex.invokerCodeArgIndex != null && argsIndex.invokerRequestArgIndex != null) {
            invokerCodeArg = (InvokerTypeCode) args[argsIndex.invokerCodeArgIndex];
            invokerRequestArg = args[argsIndex.invokerRequestArgIndex];
            log.info("Invoker execute InvokerTypeCode => {}.", invokerCodeArg.invokerCode());
            return invokersDispatcher.invoke(invokerCodeArg, invokerRequestArg);
        }
        argsIndex = new ArgsIndex();
        int parametersAnnotationsLength = parametersAnnotations.length;
        for (int i = 0; i < parametersAnnotationsLength; i++) {
            Annotation[] parameterAnnotations = parametersAnnotations[i];
            int parameterAnnotationsLength = parameterAnnotations.length;
            if (parameterAnnotations == null || parameterAnnotationsLength <= 0) {
                continue;
            }
            for (int j = 0; j < parameterAnnotationsLength; j++) {
                Annotation parameterAnnotation = parameterAnnotations[j];
                if (parameterAnnotation instanceof InvokerCode) {
                    invokerCodeArg = (InvokerTypeCode) args[i];
                    argsIndex.invokerCodeArgIndex = i;
                }
                if (parameterAnnotation instanceof InvokerRequest) {
                    invokerRequestArg = args[i];
                    argsIndex.invokerRequestArgIndex = i;
                }
            }
        }
        argsIndexMappers.put(methodName, argsIndex);
        log.info("Invoker execute InvokerTypeCode : {}", invokerCodeArg.invokerCode());
        return invokersDispatcher.invoke(invokerCodeArg, invokerRequestArg);
    }
}
