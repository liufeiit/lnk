package io.lnk.lookup.zookeeper.notify;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月19日 下午2:57:38
 */
public interface NotifyHandler {
    NotifyHandler NULL = new NotifyHandler() {
        public boolean receiveChildNotify() {
            return false;
        }
        public void handleNotify(NotifyEvent notifyEvent, NotifyMessage message) throws Throwable {}
    };
    /**
     * 是否接收子节点消息通知
     */
    boolean receiveChildNotify();
    
    /**
     * 处理接收的事件通知消息
     */
    void handleNotify(NotifyEvent notifyEvent, NotifyMessage message) throws Throwable;
}
