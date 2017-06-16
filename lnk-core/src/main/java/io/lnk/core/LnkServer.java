package io.lnk.core;

import io.lnk.api.ServiceGroup;
import io.lnk.api.exception.LnkException;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午12:56:14
 */
public interface LnkServer {
    void registry(String serviceGroup, String serviceId, String version, int protocol, Object bean) throws LnkException;
    void unregistry(String serviceGroup, String serviceId, String version, int protocol) throws LnkException;
    void bind(ServiceGroup... serviceGroups);
    void start();
    boolean isStarted();
    void shutdown();
}
