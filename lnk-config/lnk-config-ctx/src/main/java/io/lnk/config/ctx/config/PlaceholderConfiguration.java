package io.lnk.config.ctx.config;

import java.io.File;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.Ordered;

import io.lnk.config.ctx.utils.FileUtils;

/**
 * 属性替换配置器类。
 */
public class PlaceholderConfiguration extends PropertyPlaceholderConfigurer {
    private static final Logger log = LoggerFactory.getLogger(PlaceholderConfiguration.class.getSimpleName());
    private String systemId;
    private String env;
    private String configHome;
    private Properties properties;

    public boolean isProduction() {
        return env.equals("production") || env.equals("prod");
    }

    public String getConfigHome() {
        return configHome;
    }

    protected Properties loadProperties(String systemId) {
        String configHome = ConfigHome.getDir();
        configHome = configHome + "/" + systemId;
        File configDir = new File(configHome);
        if (configDir.exists() == false) {
            throw new RuntimeException("Not found the configDir=[" + configHome + "].");
        }
        Properties envProps = new Properties();
        for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
            envProps.put(entry.getKey(), entry.getValue());
        }
        File envFile = new File(configHome + "/" + "env");
        env = FileUtils.readLine(envFile).trim();
        configHome = configHome + "/" + env;
        configDir = new File(configHome);
        this.configHome = configHome;
        envProps.setProperty("config-home", configHome);
        Properties props = new Properties();
        PropertiesFileLoader.load(configDir, props, envProps);
        log.info("Load env properties, systemId=[{}].", systemId);
        return props;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
        properties = loadProperties(systemId);
        super.setProperties(properties);
    }

    public String getEnv() {
        return env;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
