package io.lnk.api.exception;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午6:27:51
 */
public class NotFoundServiceException extends RuntimeException {

    private static final long serialVersionUID = -961521687990498505L;

    public NotFoundServiceException(String serviceId) {
        this(serviceId, null);
    }

    public NotFoundServiceException(String serviceId, Throwable cause) {
        super("can't found serviceId<" + serviceId + ">", cause);
    }

}
