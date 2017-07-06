package io.lnk.api.exception;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月6日 下午5:30:34
 */
public class AppBizException extends Exception {
    private static final long serialVersionUID = -1662113710396548644L;

    public AppBizException() {
        super();
    }

    public AppBizException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppBizException(String message) {
        super(message);
    }

    public AppBizException(Throwable cause) {
        super(cause);
    }
}
