package io.lnk.demo;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 下午10:07:58
 */
public class AppBizException extends RuntimeException {
    private static final long serialVersionUID = -4764386664851944615L;

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
