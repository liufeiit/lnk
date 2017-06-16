package io.lnk.api;

import io.lnk.api.InvokerCommand;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午2:09:10
 */
public interface InvokerCallback {
    void onComplete(InvokerCommand command);
    void onError(Throwable e);
}
