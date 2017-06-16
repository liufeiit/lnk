package io.lnk.remoting.utils;

import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月21日 下午2:15:24
 */
public class RemotingThreadFactory {

    private static final Logger log = LoggerFactory.getLogger(RemotingThreadFactory.class.getSimpleName());

    public static ThreadFactory newThreadFactory(String namingPattern, boolean daemon) {
        return newThreadFactory(namingPattern, daemon, getDefaultUncaughtExceptionHandler());
    }

    public static ThreadFactory newThreadFactory(String namingPattern, boolean daemon, Thread.UncaughtExceptionHandler handler) {
        return new BasicThreadFactory.Builder().namingPattern(namingPattern).daemon(daemon).priority(Thread.NORM_PRIORITY).uncaughtExceptionHandler(handler).build();
    }

    private static Thread.UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (defaultUncaughtExceptionHandler == null) {
            defaultUncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable e) {
                    log.error("Thread named[" + t.getName() + "] running uncaught Error.", e);
                }
            };
        }
        return defaultUncaughtExceptionHandler;
    }
}
