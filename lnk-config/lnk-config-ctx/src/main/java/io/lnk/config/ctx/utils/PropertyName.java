package io.lnk.config.ctx.utils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月5日 上午10:40:08
 */
public class PropertyName {
    public static String capitalize(String str) {
        if (str.length() == 0) {
            return str;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(Character.toUpperCase(str.charAt(0)));
        sb.append(str.substring(1));
        return sb.toString();
    }

    public static String unixStyleToJavaStyle(String propName) {
        StringBuffer javaStyle = new StringBuffer();
        StringBuffer unixStyle = new StringBuffer(propName);
        int fromIndex = 0;
        int idx;
        do {
            idx = unixStyle.indexOf("-", fromIndex);
            int toIndex;
            if (idx < 0) {
                toIndex = unixStyle.length();
            } else {
                toIndex = idx;
            }
            String word = unixStyle.substring(fromIndex, toIndex);
            if (fromIndex > 0) {
                word = capitalize(word);
            }
            javaStyle.append(word);
            fromIndex = idx + 1;
        } while (idx >= 0);
        return javaStyle.toString();
    }
}
