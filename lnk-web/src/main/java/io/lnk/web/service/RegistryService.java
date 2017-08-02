package io.lnk.web.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.lnk.api.Address;
import io.lnk.api.registry.Registry;
import io.lnk.lookup.Paths;
import io.lnk.lookup.zookeeper.ZooKeeperService;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月28日 下午8:26:59
 */
@Service
public class RegistryService {
    private static final Logger log = LoggerFactory.getLogger(RegistryService.class.getSimpleName());
    @Autowired private ZooKeeperService zooKeeperService;
    @Autowired private Registry registry;

    public Map<String, List<String>> getServerList(String serviceId) {
        try {
            Map<String, List<String>> servers = new HashMap<String, List<String>>();
            String path = Paths.Registry.createPath(serviceId);
            List<String> versionList = this.zooKeeperService.getChildren(path);
            if (CollectionUtils.isEmpty(versionList) == false) {
                for (String version : versionList) {
                    path = Paths.Registry.createPath(serviceId, version);
                    List<String> protocolList = this.zooKeeperService.getChildren(path);
                    if (CollectionUtils.isEmpty(protocolList) == false) {
                        for (String protocol : protocolList) {
                            path = Paths.Registry.createPath(serviceId, version, NumberUtils.toInt(protocol));
                            List<String> serverList = this.zooKeeperService.getChildren(path);
                            servers.put(version + "/" + protocol, serverList);
                        }
                    }
                }
            }
            return servers;
        } catch (Throwable e) {
            log.error("getServerList by serviceId[" + serviceId + "] Error.", e);
        }
        return Collections.emptyMap();
    }

    public List<String> getServerList(String serviceId, String version, int protocol) {
        try {
            return this.zooKeeperService.getChildren(Paths.Registry.createPath(serviceId, version, protocol));
        } catch (Throwable e) {
            log.error("getServerList by serviceId[" + serviceId + "#" + version + "#" + protocol + "] Error.", e);
        }
        return Collections.emptyList();
    }

    public void registry(String serviceId, String version, int protocol, Address addr) {
        try {
            this.registry.registry(serviceId, version, protocol, addr);
        } catch (Throwable e) {
            log.error("registry serviceId[" + serviceId + "#" + version + "#" + protocol + "] Error.", e);
        }
    }

    public void unregistry(String serviceId, String version, int protocol, Address addr) {
        try {
            this.registry.unregistry(serviceId, version, protocol, addr);
        } catch (Throwable e) {
            log.error("unregistry serviceId[" + serviceId + "#" + version + "#" + protocol + "] Error.", e);
        }
    }
}
