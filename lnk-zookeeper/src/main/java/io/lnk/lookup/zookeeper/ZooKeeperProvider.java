package io.lnk.lookup.zookeeper;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import io.lnk.api.URI;
import io.lnk.api.utils.LnkThreadFactory;
import io.lnk.lookup.zookeeper.ZooKeeperClient.Credentials;
import io.lnk.lookup.zookeeper.notify.NotifyHandler;
import io.lnk.lookup.zookeeper.notify.NotifyMessage;
import io.lnk.lookup.zookeeper.utils.ZooKeeperUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午6:06:52
 */
public final class ZooKeeperProvider implements Command {
    protected final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());
    static {
        System.setProperty("zookeeper.disableAutoWatchReset", String.valueOf(false));
    }
    private static final String NODE_PATH_SPL = "/";
    private static final String CHARSET = "charset";
    private static final String SESSION_TIMEOUT_MILLIS = "sessionTimeoutMillis";
    private static final String CONNECT_TIMEOUT_MILLIS = "connectTimeoutMillis";
    protected final URI uri;
    private final ZooKeeperClient client;
    private final int connectTimeoutMillis;
    private final int sessionTimeoutMillis;
    private final String connectString;
    private final Charset charset;
    private final byte[] defaultData;
    final ScheduledThreadPoolExecutor executor;
    private final Map<String, NotifyHandler> notifyHandlers = new HashMap<String, NotifyHandler>();

    public ZooKeeperProvider(URI uri) {
        this(uri, uri.getInt(CONNECT_TIMEOUT_MILLIS, 30000), uri.getInt(SESSION_TIMEOUT_MILLIS, 30000), uri.getAddress());
    }

    public ZooKeeperProvider(URI uri, int connectionTimeoutMillis, int sessionTimeoutMillis, String connectString) {
        this(uri, connectionTimeoutMillis, sessionTimeoutMillis, connectString, uri.getCharset(CHARSET, Charsets.UTF_8));
    }

    public ZooKeeperProvider(URI uri, int connectTimeoutMillis, int sessionTimeoutMillis, String connectString, Charset charset) {
        super();
        this.uri = uri;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.sessionTimeoutMillis = sessionTimeoutMillis;
        this.connectString = connectString;
        this.charset = charset;
        this.defaultData = StringUtils.EMPTY.getBytes(charset);
        this.executor = new ScheduledThreadPoolExecutor(2, LnkThreadFactory.newThreadFactory("ZooKeeperProvider-%d", false));
        this.client = this.buildClient();
    }

    private final ZooKeeperClient buildClient() {
        ZooKeeperClient zkClient = new ZooKeeperClient(sessionTimeoutMillis, Credentials.NONE, connectString);
        zkClient.registerExpirationHandler(this);
        zkClient.registerCloseEventExpirationHandler(this);
        log.info("build ZooKeeperClient connection to connect uri : {}", connectString);
        return zkClient;
    }

    @Override
    public void execute() throws RuntimeException {
        executor.submit(new Runnable() {
            public void run() {
                if (notifyHandlers.isEmpty()) {
                    return;
                }
                for (int i = 0; i < 30; i++) {
                    for (Map.Entry<String, NotifyHandler> e : notifyHandlers.entrySet()) {
                        registerHandler(e.getKey(), e.getValue());
                    }
                }
            }
        });
    }

    public void create(String path, CreateMode createMode) {
        try {
            this.client.get(connectTimeoutMillis).create(path, defaultData, Ids.OPEN_ACL_UNSAFE, createMode);
        } catch (Throwable e) {
            log.error("create path : " + path + " Error.", e);
        }
    }

    public void delete(String path) {
        try {
            this.client.get(connectTimeoutMillis).delete(path, ZooKeeperUtils.ANY_VERSION);
        } catch (Throwable e) {
            log.error("remove path : " + path + " Error.", e);
        }
    }

    public boolean exists(String path) {
        try {
            return (this.client.get(connectTimeoutMillis).exists(path, true) != null);
        } catch (Throwable e) {
            log.error("exists path : " + path + " Error.", e);
        }
        return false;
    }

    public String pull(String path) {
        try {
            byte[] data = this.client.get(connectTimeoutMillis).getData(path, true, null);
            if (ArrayUtils.isEmpty(data)) {
                return null;
            }
            return new String(data, charset);
        } catch (Throwable e) {
            log.error("pull NotifyMessage Error.", e);
        }
        return null;
    }

    public List<String> getChildren(String path) {
        try {
            return this.client.get(connectTimeoutMillis).getChildren(path, true);
        } catch (Throwable e) {
            log.error("get children path Error.", e);
        }
        return null;
    }

    public void push(NotifyMessage message) {
        try {
            String path = message.getPath();
            String data = message.getData();
            CreateMode createMode = CreateMode.PERSISTENT;
            if (NotifyMessage.MessageMode.EPHEMERAL.equals(message.getMessageMode())) {
                createMode = CreateMode.EPHEMERAL;
            }
            byte[] dataBytes = data.getBytes(charset);
            ZooKeeper zooKeeper = this.client.get(connectTimeoutMillis);
            if (zooKeeper.exists(path, true) == null) {
                String[] nodes = StringUtils.split(path, NODE_PATH_SPL);
                String nodeTmp = StringUtils.EMPTY;
                for (String n : nodes) {
                    nodeTmp += (NODE_PATH_SPL.concat(n));
                    if (zooKeeper.exists(nodeTmp, true) == null) {
                        zooKeeper.create(nodeTmp, dataBytes, Ids.OPEN_ACL_UNSAFE, createMode);
                    }
                }
            }
            zooKeeper.setData(path, dataBytes, ZooKeeperUtils.ANY_VERSION);
        } catch (Throwable e) {
            log.error("push NotifyMessage Error.", e);
        }
    }

    public void registerHandler(String listenNode, NotifyHandler handler) {
        try {
            NotifyWatcher watcher = new NotifyWatcher(listenNode, handler, this);
            this.client.register(watcher);
            ZooKeeper zooKeeper = this.client.get(connectTimeoutMillis);
            Stat stat = zooKeeper.exists(listenNode, watcher);
            if (stat != null) {
                registerChildrenHandler(listenNode, zooKeeper, watcher);
            }
            log.info("register Watcher Handler : {}", listenNode);
        } catch (Throwable e) {
            log.error("register Watcher Handler : " + listenNode + " Error.", e);
        } finally {
            notifyHandlers.put(listenNode, handler);
        }
    }

    protected void registerChildrenHandler(String listenNode, ZooKeeper zooKeeper, NotifyWatcher watcher) {
        try {
            List<String> childPathList = zooKeeper.getChildren(listenNode, watcher);
            if (childPathList == null || childPathList.isEmpty()) {
                return;
            }
            for (String childPath : childPathList) {
                String childAbsPath = listenNode.concat(NODE_PATH_SPL).concat(childPath);
                zooKeeper.exists(childAbsPath, watcher);
                log.info("register Children Watcher Handler : {}", childAbsPath);
                registerChildrenHandler(childAbsPath, zooKeeper, watcher);
            }
            log.info("register Children Watcher Handler : {}", listenNode);
        } catch (Throwable e) {
            log.error("register Watcher Handler : " + listenNode + " Error.", e);
        }
    }

    public void unregister(String listenNode, NotifyHandler handler) {
        this.client.unregister(new NotifyWatcher(listenNode, handler, this));
    }
}
