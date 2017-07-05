package io.lnk.config.ctx.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.util.PropertyPlaceholderHelper;

import io.lnk.config.ctx.utils.FileUtils;

/**
 * 属性文件加载器类。
 */
public class PropertiesFileLoader {

    public static void load(File configDir, Properties props, Properties envProps) {
        try {
            loadProperties(props, configDir);
            for (Entry<Object, Object> entry : props.entrySet()) {
                String name = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (value.trim().endsWith("#fix")) {
                    int idx = value.lastIndexOf("#fix");
                    if (idx >= 0) {
                        value = value.substring(0, idx).trim();
                    }
                }
                if (value.trim().endsWith("#sec")) {
                    int idx = value.lastIndexOf("#sec");
                    if (idx >= 0) {
                        value = value.substring(0, idx).trim();
                        value = PropertyCryptoUtil.decrypt(value);
                    }
                }
                if (envProps != null) {
                    PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");
                    value = helper.replacePlaceholders(value, envProps);
                }
                props.setProperty(name, value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void loadProperties(Properties props, File configDir) throws FileNotFoundException, IOException {
        File[] files = configDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                loadProperties(props, file);
                continue;
            }
            if (file.getName().endsWith(".props")) {
                FileUtils.loadProperties(props, file);
            }
        }
    }
}
