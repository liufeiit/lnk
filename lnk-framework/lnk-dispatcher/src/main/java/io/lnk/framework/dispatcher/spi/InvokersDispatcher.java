package io.lnk.framework.dispatcher.spi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月2日 下午1:29:14
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class InvokersDispatcher {
    private static final Logger log = LoggerFactory.getLogger(InvokersDispatcher.class.getSimpleName());
    private final ConcurrentHashMap<String, Invoker> invokers = new ConcurrentHashMap<String, Invoker>();
    
    public <R, T, E extends Throwable> T invoke(InvokerTypeCode invokerTypeCode, R request) throws E {
        Invoker<Object, Object, Object, Object, E> invoker = invokers.get(invokerTypeCode.invokerCode());
        Object response = null;
        Object internalResponse = null;
        Object internalRequest = null;
        try {
            internalRequest = invoker.initialize(request);
            internalRequest = invoker.onStart(internalRequest);
            internalResponse = invoker.invoke(internalRequest);
            invoker.onComplete(internalResponse);
            return (T) invoker.onCompleteReturn(internalResponse);
        } catch (Throwable t) {
            log.error("InvokersDispatcher#invoke [InvokerTypeCode:" + invokerTypeCode.invokerCode() + "] Error.", t);
            internalResponse = invoker.onFailure(request, t);
            return (T) invoker.onFailureReturn(internalResponse, request, t);
        } finally {
            invoker.onFinally(request, response, internalRequest, internalResponse);
        }
    }
    
    public void setInvokers(Map<String, Invoker> invokers) {
        if (CollectionUtils.isEmpty(invokers)) {
            return;
        }
        for (Map.Entry<String, Invoker> e : invokers.entrySet()) {
            String actionCode = e.getKey();
            log.info("Setup Invoker named : {}", actionCode);
            this.invokers.putIfAbsent(actionCode, e.getValue());
        }
    }
}
