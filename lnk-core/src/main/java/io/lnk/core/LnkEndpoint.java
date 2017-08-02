package io.lnk.core;

import io.lnk.api.InvokerCommand;
import io.lnk.api.ServiceGroup;
import io.lnk.api.exception.LnkException;
import io.lnk.api.exception.LnkTimeoutException;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月6日 上午10:17:48
 */
public interface LnkEndpoint {
    void registry(String serviceId, String version, int protocol, Object bean) throws LnkException;
    void unregistry(String serviceId, String version, int protocol) throws LnkException;
    void bind(ServiceGroup... serviceGroups);
    
    InvokerCommand sync(final InvokerCommand command, final long timeoutMillis) throws LnkException, LnkTimeoutException;
    void async(final InvokerCommand command) throws LnkException, LnkTimeoutException;
    void multicast(final InvokerCommand command);
    
    void start();
    boolean isStarted();
    void shutdown();
}
