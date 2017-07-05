package io.lnk.lookup.zookeeper;

import java.util.List;

import org.apache.zookeeper.CreateMode;

import io.lnk.lookup.zookeeper.notify.NotifyHandler;
import io.lnk.lookup.zookeeper.notify.NotifyMessage;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月4日 下午5:53:57
 */
public interface ZooKeeperService {
    void create(String path, CreateMode createMode);
    void delete(String path);
    boolean exists(String path);
    String pull(String path);
    List<String> getChildren(String path);
    void push(NotifyMessage message);
    void registerHandler(String listenNode, NotifyHandler handler);
    void unregister(String listenNode, NotifyHandler handler);
}
