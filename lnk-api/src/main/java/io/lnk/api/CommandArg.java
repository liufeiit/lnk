package io.lnk.api;

import java.io.Serializable;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月1日 下午1:30:11
 */
public class CommandArg implements Serializable {
    private static final long serialVersionUID = 7960470799984378957L;
    private Class<?> type;
    private byte[] arg;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public byte[] getArg() {
        return arg;
    }

    public void setArg(byte[] arg) {
        this.arg = arg;
    }

    @Override
    public String toString() {
        return "CommandArg[" + type + "]";
    }
}
