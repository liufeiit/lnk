package io.lnk.api.app;

import java.io.Serializable;
import java.util.Map;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月23日 上午11:37:24
 */
public class Application implements Serializable {
    private static final long serialVersionUID = 4119167501893693281L;
    private String app;
    private String type;
    private Map<String, String> parameters;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
