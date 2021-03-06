package io.lnk.lookup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import io.lnk.api.Address;
import io.lnk.api.registry.Registry;
import io.lnk.api.utils.LnkThreadFactory;
import io.lnk.lookup.zookeeper.ZooKeeperService;
import io.lnk.lookup.zookeeper.notify.NotifyEvent;
import io.lnk.lookup.zookeeper.notify.NotifyHandler;
import io.lnk.lookup.zookeeper.notify.NotifyMessage;
import io.lnk.lookup.zookeeper.notify.NotifyMessage.MessageMode;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月19日 下午3:18:53
 */
public class ZooKeeperRegistry implements Registry {
    private static final Logger log = LoggerFactory.getLogger(ZooKeeperRegistry.class.getSimpleName());
    private final Set<NotifyMessage> notifyMessages = new HashSet<NotifyMessage>();
    private final ConcurrentHashMap<String, Set<String>> registryServices;
    private final ReentrantLock lock = new ReentrantLock();
    private final ScheduledThreadPoolExecutor recoveryExecutor;
    private ZooKeeperService zooKeeperService;
    
    public class RegistryNotifyHandler implements NotifyHandler {
        public boolean receiveChildNotify() {
            return true;
        }
        public void handleNotify(NotifyEvent notifyEvent, NotifyMessage message) throws Throwable {
            String path = message.getPath();
            if (StringUtils.contains(path, Paths.Registry.PROVIDERS) == false) {
                return;
            }
            int serverIndex = StringUtils.indexOf(path, Paths.Registry.PROVIDERS);
            path = StringUtils.substring(path, 0, serverIndex + Paths.Registry.PROVIDERS.length());
            List<String> serverList = zooKeeperService.getChildren(path);
            if (serverList == null || serverList.isEmpty()) {
                return;
            }
            registryServices.put(path, new TreeSet<String>(serverList));
        }
    }

    public ZooKeeperRegistry() {
        this.registryServices = new ConcurrentHashMap<String, Set<String>>();
        this.recoveryExecutor = new ScheduledThreadPoolExecutor(2, LnkThreadFactory.newThreadFactory("ZooKeeperRegistry.Recovery-%d", false));
        this.recoveryExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                if (CollectionUtils.isEmpty(notifyMessages)) {
                    return;
                }
                for (NotifyMessage notifyMessage : notifyMessages) {
                    zooKeeperService.push(notifyMessage);
                }
            }
        }, 10L, 10L, TimeUnit.SECONDS);
    }

    @Override
    public Address[] lookup(String serviceId, String version, int protocol) {
        SortedSet<Address> addrList = new TreeSet<Address>();
        String path = null;
        try {
            path = Paths.Registry.createPath(serviceId, version, protocol);
            Set<String> serverList = this.getServerList(path);
            if (serverList != null && !serverList.isEmpty()) {
                for (String server : serverList) {
                    addrList.add(new Address(server));
                }
            }
        } catch (Throwable e) {
            log.error("lookup path : " + path + " Error.", e);
        }
        return addrList.toArray(new Address[addrList.size()]);
    }

    @Override
    public void registry(String serviceId, String version, int protocol, Address addr) {
        String path = null;
        try {
            path = Paths.Registry.createPath(serviceId, version, protocol);
            NotifyMessage message = new NotifyMessage();
            message.setPath(path);
            message.setData(StringUtils.EMPTY);
            message.setMessageMode(MessageMode.PERSISTENT);
            this.zooKeeperService.push(message);
            String server = addr.toString();
            Set<String> serverList = this.registryServices.get(path);
            if (serverList == null) {
                serverList = new TreeSet<String>();
            }
            serverList.add(server);
            this.registryServices.put(path, serverList);
            path += ("/" + server);
            message.setPath(path);
            message.setData(server);
            message.setMessageMode(MessageMode.EPHEMERAL);
            this.notifyMessages.add(message);
            this.zooKeeperService.push(message);
            this.registerHandler(serviceId, version, protocol, addr);
            log.info("registry path : {} success.", path);
        } catch (Throwable e) {
            log.error("registry path : " + path + " Error.", e);
        }
    }

    @Override
    public void unregistry(String serviceId, String version, int protocol, Address addr) {
        String path = null;
        try {
            path = Paths.Registry.createPath(serviceId, version, protocol);
            String server = addr.toString();
            Set<String> serverList = this.registryServices.get(path);
            if (CollectionUtils.isEmpty(serverList) == false) {
                serverList.remove(server);
            }
            this.registryServices.put(path, serverList);
            this.zooKeeperService.unregister(path, NotifyHandler.NULL);
            path += ("/" + server);
            this.zooKeeperService.delete(path);
            log.warn("unregistry path : {} success.", path);
        } catch (Throwable e) {
            log.error("unregistry path : " + path + " Error.", e);
        }
    }

    private Set<String> getServerList(String path) {
        Set<String> serverList = this.registryServices.get(path);
        if (serverList == null) {
            lock.lock();
            try {
                serverList = this.registryServices.get(path);
                if (serverList == null) {
                    List<String> onlineServers = this.zooKeeperService.getChildren(path);
                    if (onlineServers == null || onlineServers.isEmpty()) {
                        log.warn("get serverList path : {} serverList is empty.", path);
                        return serverList;
                    }
                    serverList = this.registryServices.get(path);
                    if (serverList == null) {
                        serverList = new TreeSet<String>();
                    }
                    serverList.addAll(onlineServers);
                    this.registryServices.put(path, serverList);
                }
            } catch (Exception e) {
                log.error("get serverList path : " + path + " Error.", e);
            } finally {
                lock.unlock();
            }
        }
        return serverList;
    }

    private void registerHandler(String serviceId, String version, int protocol, Address addr) {
        String path = null;
        try {
            path = Paths.Registry.createPath(serviceId, version, protocol);
            String server = addr.toString();
            Set<String> serverList = this.registryServices.get(path);
            if (serverList == null) {
                serverList = new TreeSet<String>();
            }
            serverList.add(server);
            this.registryServices.put(path, serverList);
            this.zooKeeperService.registerHandler(path, new RegistryNotifyHandler());
        } catch (Throwable e) {
            log.error("registerHandler path : " + path + " Error.", e);
        }
    }

    public void setZooKeeperService(ZooKeeperService zooKeeperService) {
        this.zooKeeperService = zooKeeperService;
    }
}
