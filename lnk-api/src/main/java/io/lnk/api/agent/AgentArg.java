package io.lnk.api.agent;

import java.io.Serializable;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 上午11:31:47
 */
public class AgentArg implements Serializable {
    private static final long serialVersionUID = -4486769029080888869L;
    private String type;
    private String arg;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

    @Override
    public String toString() {
        return "AgentArg [type=" + type + ", arg=" + arg + "]";
    }
}