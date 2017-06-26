package io.lnk.track;

import org.apache.commons.lang3.StringUtils;

import io.lnk.api.InvokerCommand;
import io.lnk.api.app.Application;
import io.lnk.api.track.Tracker;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月26日 下午6:07:46
 */
public class NestedTracker implements Tracker {
    private final Tracker tracker;

    public NestedTracker(String type) {
        super();
        if (StringUtils.equals(type, "logger")) {
            this.tracker = new LogTracker();
        } else {
            this.tracker = new LogTracker();
        }
    }

    @Override
    public void trackInvokeBefore(InvokerCommand command, Application application) {
        this.tracker.trackInvokeBefore(command, application);
    }

    @Override
    public void trackInvokeAfter(InvokerCommand command, Application application) {
        this.tracker.trackInvokeAfter(command, application);
    }
}
