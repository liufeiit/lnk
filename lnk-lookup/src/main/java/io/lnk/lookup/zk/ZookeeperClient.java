package io.lnk.lookup.zk;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import io.lnk.api.URI;

public abstract class ZookeeperClient {
    protected static final Logger log = LoggerFactory.getLogger(ZookeeperClient.class.getSimpleName());
    private CuratorFramework client;
    private int connectionTimeoutMs;
    private int sessionTimeoutMs;
    private String connectServer;
    private HashSet<String> watchers = new HashSet<String>();
    private String namespace;
    private final URI uri;
    private Charset charset = Charsets.UTF_8;

    public ZookeeperClient(URI uri, String namespace) {
        this(uri, 30000, 30000, namespace);
    }

    public ZookeeperClient(URI uri, int connectionTimeout, int sessionTimeout, String namespace) {
        this.uri = uri;
        String charsetName = this.uri.getParameters().get("charset");
        if (StringUtils.isNotBlank(charsetName)) {
            this.charset = Charset.forName(charsetName);
        }
        this.connectServer = this.uri.getAddress();
        this.connectionTimeoutMs = connectionTimeout;
        this.sessionTimeoutMs = sessionTimeout;
        this.namespace = namespace;
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 29);
        client = createWithOptions(connectServer, retryPolicy, connectionTimeout, sessionTimeout, namespace);
        client.start();
    }

    private CuratorFramework createWithOptions(String connectionString, RetryPolicy retryPolicy, int connectionTimeoutMs, int sessionTimeoutMs, String namespace) {
        return CuratorFrameworkFactory.builder().connectString(connectionString).namespace(namespace).retryPolicy(retryPolicy).connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs).build();
    }

    protected boolean createEphemeralNode(String path, String data) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, data.getBytes(this.charset));
            return true;
        } catch (Throwable e) {
            log.warn("createEphemeralNode Error.", e);
        }
        return false;
    }

    protected boolean createPersistentNode(String path, String data) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, data.getBytes(this.charset));
            return true;
        } catch (Throwable e) {
            log.warn("createPersistentNode Error.", e);
        }
        return false;
    }

    protected List<String> getChildrenList(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (Throwable e) {
            log.warn("getChildrenList Error.", e);
        }
        return Collections.emptyList();
    }

    protected List<String> getChildrenListWithWatcher(String path, ChildListener listener) {
        try {
            ZKWatchChildren watcher = new ZKWatchChildren(listener);
            List<String> childrens = client.getChildren().usingWatcher(watcher).forPath(path);
            addReconnectionWatcher(path, ZookeeperWatcherType.GET_CHILDREN, watcher);
            return childrens;
        } catch (Throwable e) {
            log.warn("getChildrenListWithWatcher Error.", e);
        }
        return Collections.emptyList();
    }

    protected boolean checkNodeExist(String path) {
        try {
            return client.checkExists().forPath(path) != null;
        } catch (Throwable e) {
            log.warn("checkNodeExist Error.", e);
        }
        return false;
    }

    protected boolean checkPathExistWithWatcher(String path, final CuratorWatcher watcher) {
        try {
            return client.checkExists().usingWatcher(watcher).forPath(path) != null;
        } catch (Throwable e) {
            log.warn("checkNodeExist Error.", e);
        }
        return false;
    }

    protected boolean setData(String path, String payload) {
        try {
            client.setData().forPath(path, payload.getBytes(this.charset));
            return true;
        } catch (Throwable e) {
            log.warn("setData Error.", e);
        }
        return false;
    }

    protected boolean deletePath(String path) {
        try {
            client.delete().forPath(path);
            return true;
        } catch (Throwable e) {
            log.warn("deletePath Error.", e);
        }
        return false;
    }

    protected boolean deletePathTree(String path) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
            return true;
        } catch (Throwable e) {
            log.warn("deletePathTree Error.", e);
        }
        return false;
    }

    protected boolean guaranteedDeletePathTree(String path) {
        try {
            client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
            return true;
        } catch (Throwable e) {
            log.warn("guaranteedDeletePathTree Error.", e);
        }
        return false;
    }

    protected String getData(String path) {
        try {
            return new String(client.getData().forPath(path), this.charset);
        } catch (Throwable e) {
            log.warn("getData Error", e);
        }
        return StringUtils.EMPTY;
    }

    protected String getDataWithWatcher(String path, final DataListener listener) {
        try {
            ZKWatchData watcher = new ZKWatchData(listener);
            byte[] buffer = client.getData().usingWatcher(watcher).forPath(path);
            addReconnectionWatcher(path, ZookeeperWatcherType.GET_DATA, watcher);
            return new String(buffer, this.charset);
        } catch (Throwable e) {
            log.warn("getDataWithWatcher Error.", e);
        }
        return StringUtils.EMPTY;
    }

    private void addReconnectionWatcher(final String path, final ZookeeperWatcherType watcherType, final CuratorWatcher watcher) {
        synchronized (this) {
            if (!watchers.contains(watcher.toString())) {
                watchers.add(watcher.toString());
                client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                    public void stateChanged(CuratorFramework client, ConnectionState newState) {
                        if (newState == ConnectionState.RECONNECTED) {
                            int tryCount = 0;
                            while (tryCount++ < 3) {
                                try {
                                    if (watcherType == ZookeeperWatcherType.EXITS) {
                                        client.checkExists().usingWatcher(watcher).forPath(path);
                                    } else if (watcherType == ZookeeperWatcherType.GET_CHILDREN) {
                                        client.getChildren().usingWatcher(watcher).forPath(path);
                                    } else if (watcherType == ZookeeperWatcherType.GET_DATA) {
                                        client.checkExists().usingWatcher(watcher).forPath(path);
                                    } else if (watcherType == ZookeeperWatcherType.CREATE_ON_NO_EXITS) {
                                        if (ZookeeperClient.this.isServiceActive(path)) {
                                            Stat stat = client.checkExists().usingWatcher(watcher).forPath(path);
                                            if (stat == null) {
                                                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path);
                                            }
                                        }
                                    }
                                    break;
                                } catch (InterruptedException e) {
                                    log.warn("path:" + path + ", zookeeper session timeout stateChanged handle InterruptedException:", e);
                                    break;
                                } catch (Throwable e) {
                                    log.warn("path:" + path + ", zookeeper session timeout stateChanged handle exception:", e);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    public String getServer() {
        return this.connectServer;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeoutMs;
    }

    public int getSessionTimeout() {
        return this.sessionTimeoutMs;
    }

    protected void register(String path, String data) throws Exception {
        CuratorWatcher watcher = new ZKWatchRegister(path, data);
        Stat stat = client.checkExists().forPath(path);
        if (stat != null) {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        }
        client.checkExists().usingWatcher(watcher).forPath(path);
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path, data.getBytes(this.charset));
        addReconnectionWatcher(path, ZookeeperWatcherType.CREATE_ON_NO_EXITS, watcher);
    }

    public abstract boolean isServiceActive(String path);

    private class ZKWatchRegister implements CuratorWatcher {
        private final String path;
        private byte[] value;

        @SuppressWarnings("unused")
        public String getPath() {
            return path;
        }

        public ZKWatchRegister(String path, String value) {
            this.path = path;
            this.value = value.getBytes(charset);
        }

        public void process(WatchedEvent event) throws Exception {
            try {
                if (event.getType() == EventType.NodeDataChanged) {
                    byte[] data = client.getData().usingWatcher(this).forPath(path);
                    value = data;
                } else if (event.getType() == EventType.NodeDeleted) {
                    if (ZookeeperClient.this.isServiceActive(event.getPath())) {
                        Stat stat = client.checkExists().usingWatcher(this).forPath(path);
                        if (stat == null) {
                            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path);
                        }
                    }
                } else if (event.getType() == EventType.NodeCreated) {
                    client.setData().forPath(path, value);
                    client.checkExists().usingWatcher(this).forPath(path);
                }
            } catch (Throwable e) {
                log.warn("process event Error : {}", e.getLocalizedMessage());
            }
        }
    }

    private class ZKWatchChildren implements CuratorWatcher {
        private volatile ChildListener listener;

        public ZKWatchChildren(ChildListener listener) {
            this.listener = listener;
        }

        public void process(WatchedEvent event) throws Exception {
            if (event.getType() == EventType.NodeChildrenChanged) {
                if (!ZookeeperClient.this.checkNodeExist(event.getPath())) {
                    ZookeeperClient.this.createPersistentNode(event.getPath(), "");
                }
                List<String> childrens = client.getChildren().usingWatcher(this).forPath(event.getPath());
                if (listener != null) {
                    listener.childChanged(event.getPath(), childrens);
                }
            } else if (event.getType() == EventType.None && event.getPath() == null) {
                log.info("ZKWatchChildren zookeeper session timeout");
            } else {
                client.checkExists().usingWatcher(this).forPath(event.getPath());
            }
        }
    }

    private class ZKWatchData implements CuratorWatcher {
        private volatile DataListener listener;

        public ZKWatchData(DataListener listener) {
            this.listener = listener;
        }

        public void process(WatchedEvent event) throws Exception {
            if (event.getType() == EventType.NodeDataChanged) {
                if (!ZookeeperClient.this.checkNodeExist(event.getPath())) {
                    ZookeeperClient.this.createPersistentNode(event.getPath(), "{}");
                }
                byte[] data = client.getData().usingWatcher(this).forPath(event.getPath());
                if (listener != null) {
                    listener.dataChanged(event.getPath(), new String(data, charset));
                }
            } else if (event.getType() == EventType.None && event.getPath() == null) {
                log.info("ZKWatchData zookeeper session timeout");
            } else {
                client.checkExists().usingWatcher(this).forPath(event.getPath());
            }
        }
    }

    protected String createPath(String serviceGroup, String serviceId, String version, int protocol) {
        StringBuilder sb = new StringBuilder("/");
        sb.append(serviceGroup).append("/").append(serviceId).append("/").append(version).append("/").append(protocol).append("/server");
        return sb.toString();
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    protected enum ZookeeperWatcherType {
        GET_DATA, GET_CHILDREN, EXITS, CREATE_ON_NO_EXITS
    }
}
