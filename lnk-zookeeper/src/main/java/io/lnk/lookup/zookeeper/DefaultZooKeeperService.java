package io.lnk.lookup.zookeeper;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.InitializingBean;

import io.lnk.api.URI;
import io.lnk.lookup.zookeeper.notify.NotifyHandler;
import io.lnk.lookup.zookeeper.notify.NotifyMessage;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月4日 下午5:56:11
 */
public class DefaultZooKeeperService implements ZooKeeperService, InitializingBean {
    private String zookeeperUri;
    private ZooKeeperProvider provider;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.provider = new ZooKeeperProvider(URI.valueOf(this.zookeeperUri));
    }

    @Override
    public void create(String path, CreateMode createMode) {
        this.provider.create(path, createMode);
    }

    @Override
    public void delete(String path) {
        this.provider.delete(path);
    }

    @Override
    public boolean exists(String path) {
        return this.provider.exists(path);
    }

    @Override
    public String pull(String path) {
        return this.provider.pull(path);
    }

    @Override
    public List<String> getChildren(String path) {
        return this.provider.getChildren(path);
    }

    @Override
    public void push(NotifyMessage message) {
        this.provider.push(message);
    }

    @Override
    public void registerHandler(String listenNode, NotifyHandler handler) {
        this.provider.registerHandler(listenNode, handler);
    }

    @Override
    public void unregister(String listenNode, NotifyHandler handler) {
        this.provider.unregister(listenNode, handler);
    }

    public void setZookeeperUri(String zookeeperUri) {
        this.zookeeperUri = zookeeperUri;
    }
}
