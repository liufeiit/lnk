package io.lnk.config.ctx.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月5日 上午10:59:40
 */
public class DateUtils {

    public static String format(String pattern, Locale locale, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
        return sdf.format(date);
    }

    public static String format(String pattern, Date date) {
        return format(pattern, Locale.getDefault(), date);
    }
}
