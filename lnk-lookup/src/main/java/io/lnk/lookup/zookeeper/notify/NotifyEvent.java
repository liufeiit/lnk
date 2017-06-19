package io.lnk.lookup.zookeeper.notify;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月19日 下午2:56:15
 */
public enum NotifyEvent {

    /**
     * 节点创建事件
     */
    NODE_CREATED,
    
    /**
     * 节点被删除事件
     */
    NODE_DELETED,
    
    /**
     * 节点数据变化事件
     */
    NODE_DATA_CHANGED,
}