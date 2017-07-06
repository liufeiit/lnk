package io.lnk.config.ctx.ns;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.jxpath.JXPathContext;
import org.springframework.beans.factory.InitializingBean;

import io.lnk.config.ctx.config.ConfigHome;
import io.lnk.config.ctx.utils.PropertyName;

/**
 * 名字服务注册器实现类。
 */
public class NsRegistryImpl implements NsRegistry, InitializingBean {
    private static String ENV_PATH = "/environment/currentEnv";
    private SortedMap<String, String> nsMap;
    private String nsHome;

    public void afterPropertiesSet() throws Exception {
        if (nsHome == null) {
            nsHome = ConfigHome.getNsDir();
        }
        if (nsMap == null || nsMap.isEmpty()) {
            nsMap = NsFileLoader.load(nsHome);
        }
    }

    public SortedMap<String, String> getValues(String namePrefix, boolean includePrefix) {
        if (namePrefix.endsWith("/") == false && namePrefix.equals("/") == false) {
            namePrefix = namePrefix + "/";
        }
        SortedMap<String, String> subMap = nsMap.tailMap(namePrefix);
        SortedMap<String, String> retMap = new TreeMap<String, String>();
        for (Entry<String, String> entry : subMap.entrySet()) {
            if (entry.getKey().startsWith(namePrefix) == false) {
                return retMap;
            }
            String key = entry.getKey();
            if (includePrefix == false) {
                key = key.substring(namePrefix.length());
                if (key.startsWith("/")) {
                    key = key.substring(1);
                }
            }
            retMap.put(key, entry.getValue());
        }
        return retMap;
    }

    public String getValue(String name) {
        return nsMap.get(name);
    }


    public String getCurrentEnv() {
        return this.getValue(ENV_PATH);
    }

    public void setProperties(String nsPath, Object bean) {
        JXPathContext xpathContext = JXPathContext.newContext(bean);
        Map<String, String> nsValues = getValues(nsPath, false);
        for (Entry<String, String> entry : nsValues.entrySet()) {
            xpathContext.setValue(PropertyName.unixStyleToJavaStyle(entry.getKey()), entry.getValue());
        }
    }

    public void setNsMap(SortedMap<String, String> nsMap) {
        this.nsMap = nsMap;
    }

    public void setNsHome(String nsHome) {
        this.nsHome = nsHome;
    }
}
