package io.lnk.lookup.consul;

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

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午9:31:42
 */
public class ConsulLookupRegistry extends ConsulRegistry implements Registry {
    private Map<String, Set<String>> registryServices = new ConcurrentHashMap<String, Set<String>>();
    private Map<String, Long> subscribeService = new ConcurrentHashMap<String, Long>();

    public ConsulLookupRegistry(final URI uri) {
        super(uri);
    }

    @Override
    public void unregistry(String serviceGroup, String serviceId, String version, int protocol, Address addr) {
        super.doUnregister(new URL(serviceGroup, serviceId, version, protocol, addr.getHost(), addr.getPort()));
        String unregistryAddr = addr.toString();
        for (String key : registryServices.keySet()) {
            Set<String> addrSet = registryServices.get(key);
            if (addrSet != null) {
                addrSet.remove(unregistryAddr);
            }
            registryServices.put(key, addrSet);
        }
    }

    @Override
    public Address[] lookup(String serviceGroup, String serviceId, String version, int protocol) {
        URL url = new URL(serviceGroup, serviceId, version, protocol, "", 0);
        SortedSet<Address> addrList = new TreeSet<Address>();
        try {
            subscribeServiceOnce(url);
            String[] servers = URL.toAddressArray(super.discoverService(url));
            for (String server : servers) {
                addrList.add(new Address(server));
            }
        } catch (Throwable e) {
            System.out.println("lookup path:{} failed." + url.getSimpleStr());
        }
        return addrList.toArray(new Address[addrList.size()]);
    }

    @Override
    public void registry(String serviceGroup, String serviceId, String version, int protocol, Address addr) {
        URL url = new URL(serviceGroup, serviceId, version, protocol, addr.getHost(), addr.getPort());
        String path = url.getSimpleStr();
        try {
            super.doRegister(url);
            synchronized (registryServices) {
                Set<String> serverSet = registryServices.get(path);
                if (serverSet == null) {
                    serverSet = new HashSet<String>();
                    registryServices.put(path, serverSet);
                }
                serverSet.add(addr.getHost() + ":" + addr.getPort());
            }
        } catch (Throwable e) {
            log.info("registry path : " + path + " Address : " + addr + " Error.");
        }
    }

    private void subscribeServiceOnce(URL url) {
        Long value = subscribeService.get(url.getServiceGroup());
        if (value == null) {
            synchronized (this) {
                value = subscribeService.get(url.getServiceGroup());
                if (value == null) {
                    super.subscribeService(url, new ServiceListener() {
                        @Override
                        public void notifyService(URL refUrl, List<URL> urls) {

                        }

                    });
                    subscribeService.put(url.getServiceGroup(), 1L);
                }
            }
        }
    }
}
