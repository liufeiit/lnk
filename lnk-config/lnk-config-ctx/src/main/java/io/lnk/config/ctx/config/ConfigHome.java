package io.lnk.config.ctx.config;

/**
 * 配置主目录类。
 */
public class ConfigHome {
    public static final String PROPERTY_APP_CONFIG_HOME = "lnk.app.config.home";
    public static final String PROPERTY_NS_CONFIG_HOME = "lnk.ns.config.home";
    
    public static String getDir() {
        String configHome = System.getProperty(PROPERTY_APP_CONFIG_HOME);
        if (configHome == null) {
            configHome = System.getProperty("user.home") + "/.lnk_config";
        }
        return configHome;
    }

    public static String getNsDir() {
        String configHome = System.getProperty(PROPERTY_NS_CONFIG_HOME);
        if (configHome == null) {
            configHome = System.getProperty("user.home") + "/.ns_config";
        }
        return configHome;
    }
}
