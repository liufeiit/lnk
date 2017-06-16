package io.lnk.api.track;

import io.lnk.api.InvokerCommand;
import io.lnk.api.app.Application;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月23日 上午11:39:59
 */
public interface Tracker {
    void trackInvokeBefore(final InvokerCommand command, final Application application);
    void trackInvokeAfter(final InvokerCommand command, final Application application);
}
