package io.lnk.lookup.zk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import io.lnk.api.Address;
import io.lnk.api.URI;
import io.lnk.api.registry.Registry;

public class ZokLookupRegistry extends ZookeeperClient implements Registry {
    private static final String LOOKUP_NS = "lnk/rpc/ns";
    private Map<String, List<String>> cachedServers = new ConcurrentHashMap<String, List<String>>();
    private Map<String, Integer> registryServicesFlag = new ConcurrentHashMap<String, Integer>();
    private Map<String, Set<String>> registryServices = new ConcurrentHashMap<String, Set<String>>();

    public ZokLookupRegistry(final URI uri) {
        super(uri, LOOKUP_NS);
    }

    public void dumpCachedServers() {
        for (String key : cachedServers.keySet()) {
            System.out.println("key :" + key + ", value:" + cachedServers.get(key).toString());
        }
    }

    public boolean isServiceActive(String path) {
        Integer ret = registryServicesFlag.get(path);
        if (ret == null || ret == 0) {
            return false;
        }
        return true;
    }

    @Override
    public void unregistry(String serviceGroup, String serviceId, String version, int protocol) {
        for (String key : registryServicesFlag.keySet()) {
            String path = super.createPath(serviceGroup, serviceId, version, protocol);
            if (key.startsWith(path)) {
                registryServicesFlag.put(key, 0);
            }
        }
        for (String key : registryServices.keySet()) {
            Set<String> serverSet = registryServices.get(key);
            if (serverSet != null) {
                for (String server : serverSet) {
                    String delPath = key + "/" + server;
                    try {
                        deletePath(delPath);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Override
    public void registry(String serviceGroup, String serviceId, String version, int protocol, Address addr) {
        String path = super.createPath(serviceGroup, serviceId, version, protocol);
        String nodePath = path + "/" + addr.getHost() + ":" + addr.getPort();
        try {
            registryServicesFlag.put(nodePath, 1);
            register(nodePath, "");
            Set<String> serverSet = registryServices.get(path);
            if (serverSet == null) {
                serverSet = new HashSet<String>();
                registryServices.put(path, serverSet);
            }
            serverSet.add(addr.getHost() + ":" + addr.getPort());
            log.info("registry path : {} Address : {} success.", path, addr);
        } catch (Throwable e) {
            log.error("registry path : " + path + " Address : " + addr + " Error.", e);
        }
    }

    @Override
    public Address[] lookup(String serviceGroup, String serviceId, String version, int protocol) {
        SortedSet<Address> addrList = new TreeSet<Address>();
        String path = super.createPath(serviceGroup, serviceId, version, protocol);
        try {
            List<String> servers = getServers(path);
            if (servers != null && !servers.isEmpty()) {
                for (String server : servers) {
                    addrList.add(new Address(server));
                }
            }
        } catch (Throwable e) {
            log.warn("lookup path : {} Error..", path);
        }
        return addrList.toArray(new Address[addrList.size()]);
    }

    private List<String> getServers(String path) {
        List<String> servers = cachedServers.get(path);
        if (servers == null) {
            synchronized (this) {
                servers = cachedServers.get(path);
                if (servers == null) {
                    servers = getChildrenListWithWatcher(path, new ChildListener() {
                        @Override
                        public void childChanged(String path, List<String> children) {
                            if (children == null) {
                                children = new ArrayList<String>();
                            }
                            cachedServers.put(path, children);
                        }
                    });
                    if (servers == null) {
                        cachedServers.put(path, new ArrayList<String>());
                        return null;
                    }
                    cachedServers.put(path, servers);
                }
            }
        }
        return servers;
    }
}
