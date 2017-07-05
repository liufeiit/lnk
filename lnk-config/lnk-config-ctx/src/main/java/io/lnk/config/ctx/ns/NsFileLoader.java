package io.lnk.config.ctx.ns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;

import io.lnk.config.ctx.utils.FileUtils;

import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 名称服务文件加载器类。
 */
public class NsFileLoader {
    private static void loadNs(SortedMap<String, String> ns, String nsHome, String nsPath) throws FileNotFoundException, IOException {
        File configDir = new File(nsHome + nsPath);
        File[] files = configDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            String nsPath2;
            if (nsPath.equals("/")) {
                nsPath2 = nsPath + file.getName();
            } else {
                nsPath2 = nsPath + "/" + file.getName();
            }

            if (file.isDirectory()) {
                loadNs(ns, nsHome, nsPath2);
                continue;
            }

            if (file.getName().endsWith(".props")) {
                // 后缀为.props的文件，文件名不计入路径中
                nsPath2 = nsPath;
            }
            Properties props = new Properties();
            FileUtils.loadProperties(props, file);
            for (Entry<Object, Object> entry : props.entrySet()) {
                String key;
                if (nsPath2.endsWith("/")) {
                    key = nsPath2 + entry.getKey();
                } else {
                    key = nsPath2 + "/" + entry.getKey();
                }
                ns.put(key, entry.getValue().toString());
            }
        }
    }

    public static SortedMap<String, String> load(String nsHome) {
        try {
            SortedMap<String, String> map = new TreeMap<String, String>();
            File nsHomeFile = new File(nsHome);
            if (nsHomeFile.exists() == false) {
                return map;
            }
            loadNs(map, nsHome, "/");
            for (Entry<String, String> entry : map.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();

                if (value.trim().endsWith("#sec")) {
                    int idx = value.lastIndexOf("#sec");
                    if (idx >= 0) {
                        value = value.substring(0, idx).trim();
                        value = NsCryptoUtil.decrypt(value);
                    }
                }
                map.put(name, value);
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
