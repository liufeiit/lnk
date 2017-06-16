package io.lnk.api;

import java.io.Serializable;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月2日 下午6:09:11
 */
public class CommandReturnObject implements Serializable {
    private static final long serialVersionUID = 7732279502675367759L;
    private Class<?> type;
    private byte[] retObject;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public byte[] getRetObject() {
        return retObject;
    }

    public void setRetObject(byte[] retObject) {
        this.retObject = retObject;
    }
}
