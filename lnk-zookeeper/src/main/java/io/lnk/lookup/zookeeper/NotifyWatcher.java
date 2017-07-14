package io.lnk.lookup.zookeeper;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.lookup.zookeeper.notify.NotifyEvent;
import io.lnk.lookup.zookeeper.notify.NotifyHandler;
import io.lnk.lookup.zookeeper.notify.NotifyMessage;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月19日 下午2:36:25
 */
public class NotifyWatcher implements Watcher {
    private final static Logger log = LoggerFactory.getLogger(NotifyWatcher.class);
    private final String listenNode;
    private final NotifyHandler handler;
    private final ZooKeeperProvider provider;
    private final boolean receiveChildNotify;
    
    public NotifyWatcher(String listenNode, NotifyHandler handler, ZooKeeperProvider provider) {
        super();
        this.listenNode = listenNode;
        this.handler = handler;
        this.provider = provider;
        this.receiveChildNotify = this.handler.receiveChildNotify();
        this.provider.executor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                WatchedEvent event = new WatchedEvent(EventType.NodeDataChanged, KeeperState.SyncConnected, NotifyWatcher.this.listenNode);
                NotifyWatcher.this.process(event);
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void process(WatchedEvent event) {
        EventType eventType = event.getType();
        String path = event.getPath();
        if (StringUtils.isEmpty(path)) {
            return;
        }
        try {
            if (this.receiveChildNotify) {
                if (StringUtils.contains(path, listenNode) == false) {
                    return;
                }
            } else {
                if (StringUtils.equals(path, listenNode) == false) {
                    return;
                }
            }
            NotifyMessage message = new NotifyMessage();
            message.setPath(path);
            switch (eventType) {
                case None:
                    log.warn("can't handle EventType : {}", eventType);
                    break;
                case NodeCreated:
                    message.setData(provider.pull(path));
                    handler.handleNotify(NotifyEvent.NODE_CREATED, message);
                    break;
                case NodeDeleted:
                    handler.handleNotify(NotifyEvent.NODE_DELETED, message);
                    break;
                case NodeDataChanged:
                    message.setData(provider.pull(path));
                    handler.handleNotify(NotifyEvent.NODE_DATA_CHANGED, message);
                    break;
                case NodeChildrenChanged:
                    // 仅仅再次注册回调处理器即可完成对子节点的侦听
                    provider.registerHandler(path, handler);
                    break;
                default:
                    log.warn("Can't handle EventType : {}", eventType);
                    break;
            }
        } catch (Throwable e) {
            log.error("handleNotify Error.", e);
        } finally {
            // 重新注册 避免一次性回调之后Zookeeper将Watcher卸载
            // 3.4.6以后的版本无需重新注册
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((listenNode == null) ? 0 : listenNode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NotifyWatcher other = (NotifyWatcher) obj;
        if (listenNode == null) {
            if (other.listenNode != null)
                return false;
        } else if (!listenNode.equals(other.listenNode))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NotifyWatcher [listenNode=" + listenNode + "]";
    }
}
