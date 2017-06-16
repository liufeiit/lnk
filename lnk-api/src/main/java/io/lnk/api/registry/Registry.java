package io.lnk.api.registry;

import io.lnk.api.Address;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 上午11:49:43
 */
public interface Registry {
    Address[] lookup(String serviceGroup, String serviceId, String version, int protocol);
    void registry(String serviceGroup, String serviceId, String version, int protocol, Address addr);
    void unregistry(String serviceGroup, String serviceId, String version, int protocol);
}
