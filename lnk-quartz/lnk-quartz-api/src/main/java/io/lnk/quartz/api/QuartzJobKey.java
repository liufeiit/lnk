package io.lnk.quartz.api;

import java.io.Serializable;

/**
 * 定时任务键值类。
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午9:11:52
 */
public class QuartzJobKey implements Serializable {
    private static final long serialVersionUID = -7731561125545949974L;

    /**
     * 组
     */
    private String group;

    /**
     * 名称
     */
    private String name;

    public static QuartzJobKey jobKey(String name, String group) {
        QuartzJobKey key = new QuartzJobKey();
        key.name = name;
        key.group = group;

        return key;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
