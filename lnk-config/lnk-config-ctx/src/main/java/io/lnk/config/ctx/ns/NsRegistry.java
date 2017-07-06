package io.lnk.config.ctx.ns;

import java.util.SortedMap;

/**
 * 名字服务注册器接口定义类。
 */
public interface NsRegistry {
    String CURRENT_ENV_KEY = "lnk.app.current.env";
    /**
     * 获得名字前缀下的值集。
     */
    SortedMap<String, String> getValues(String namePrefix, boolean includePrefix);

    /**
     * 获得名字对应的值。
     */
    String getValue(String name);

    /**
     * 设置属性
     */
    void setProperties(String nsPath, Object bean);

    /**
     * 获取当前环境
     */
    String getCurrentEnv();
}
