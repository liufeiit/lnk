package io.lnk.quartz.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务详细类。
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午9:11:18
 */
public class QuartzJobDetail implements Serializable {
    private static final long serialVersionUID = -580037227163494369L;

    /**
     * 任务键
     */
    private QuartzJobKey key;

    /**
     * 任务属性集
     */
    private Map<String, String> properties = new HashMap<String, String>();

    public static QuartzJobDetail newJobDetail(String name, String group) {
        QuartzJobDetail jobDetail = new QuartzJobDetail();
        jobDetail.setKey(name, group);
        return jobDetail;
    }

    public QuartzJobDetail addProperty(String name, String value) {
        setProperty(name, value);
        return this;
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public void setKey(String name, String group) {
        key = new QuartzJobKey();
        key.setName(name);
        key.setGroup(group);
    }

    public QuartzJobKey getKey() {
        return key;
    }

    public void setKey(QuartzJobKey key) {
        this.key = key;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
