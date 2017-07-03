package io.lnk.lookup.zookeeper.notify;

import java.io.Serializable;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月19日 下午2:56:44
 */
public class NotifyMessage implements Serializable {
    private static final long serialVersionUID = 5334781767972692658L;
    public enum MessageMode {PERSISTENT, EPHEMERAL;}
    private String path;
    private String data;
    private MessageMode messageMode = MessageMode.PERSISTENT;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public MessageMode getMessageMode() {
        return messageMode;
    }

    public void setMessageMode(MessageMode messageMode) {
        this.messageMode = messageMode;
    }
}