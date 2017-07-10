package io.lnk.api.exception;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月6日 下午5:30:34
 */
public class AppBizRtException extends RuntimeException {
    private static final long serialVersionUID = -1662113710396548644L;
    private String code;

    public AppBizRtException() {
        super();
    }

    public AppBizRtException(String code) {
        super();
        this.code = code;
    }

    public AppBizRtException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public AppBizRtException(String code, String message) {
        super(message);
        this.code = code;
    }

    public AppBizRtException(String code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
