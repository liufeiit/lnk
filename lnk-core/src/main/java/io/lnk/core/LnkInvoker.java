package io.lnk.core;

import io.lnk.api.InvokerCallback;
import io.lnk.api.InvokerCommand;
import io.lnk.api.exception.LnkException;
import io.lnk.api.exception.LnkTimeoutException;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午12:55:54
 */
public interface LnkInvoker {
    InvokerCommand sync(final InvokerCommand command, final long timeoutMillis) throws LnkException, LnkTimeoutException;
    void async_callback(final InvokerCommand command, final long timeoutMillis, final InvokerCallback callback) throws LnkException, LnkTimeoutException;
    void async(final InvokerCommand command) throws LnkException, LnkTimeoutException;
    void multicast(final InvokerCommand command);
    void start();
    boolean isStarted();
    void shutdown();
}
