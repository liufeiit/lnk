package io.lnk.core;

import org.slf4j.MDC;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年8月6日 下午12:42:42
 */
public class MdcCaller {
    private static final String TRACKING_CODE_KEY = "trackingCode";

    public static void setTrackingCode(String value) {
        MDC.put(TRACKING_CODE_KEY, value);
    }
    
    public static void removeTrackingCode() {
        MDC.remove(TRACKING_CODE_KEY);
    }
}