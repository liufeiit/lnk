package io.lnk.api;

import java.io.Serializable;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月1日 下午1:30:11
 */
public class ProtocolObject implements Serializable {
    private static final long serialVersionUID = 7960470799984378957L;
    private Class<?> type;
    private byte[] data;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ProtocolObject [type=" + type + "]";
    }
}
