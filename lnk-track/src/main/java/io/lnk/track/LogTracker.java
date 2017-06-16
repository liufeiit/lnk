package io.lnk.track;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.api.InvokerCommand;
import io.lnk.api.app.Application;
import io.lnk.api.exception.transport.CommandTransportException;
import io.lnk.api.track.Tracker;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月25日 下午2:51:17
 */
public class LogTracker implements Tracker {
    private static final Logger log = LoggerFactory.getLogger("LnkTracker");

    @Override
    public void trackInvokeBefore(InvokerCommand command, Application application) {
        log.info("app[{}] invoke before app[{}]-service[{}]", new Object[] {command.getApplication().getApp(), application.getApp(), command.commandSignature()});
    }

    @Override
    public void trackInvokeAfter(InvokerCommand command, Application application) {
        log.info("app[{}] invoke after app[{}]-service[{}]", new Object[] {command.getApplication().getApp(), application.getApp(), command.commandSignature()});
        CommandTransportException exception = command.getException();
        if (exception != null) {
            log.error("app[{}] invoke after app[{}]-service[{}] Error[{}] : {}", new Object[] {command.getApplication().getApp(), application.getApp(), command.commandSignature(), exception.getClassName(), exception.getMessage()});
        }
    }
}
