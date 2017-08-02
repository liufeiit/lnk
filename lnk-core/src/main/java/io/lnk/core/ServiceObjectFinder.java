package io.lnk.core;

import io.lnk.api.InvokerCommand;
import io.lnk.api.exception.NotFoundServiceException;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午6:26:14
 */
public interface ServiceObjectFinder {
    Object getServiceObject(InvokerCommand command) throws NotFoundServiceException;
    void registry(String serviceId, String version, int protocol, Object bean);
}
