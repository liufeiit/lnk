package io.lnk.remoting.mina;

import io.lnk.remoting.SystemConfiguration;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月19日 下午10:39:38
 */
public class MinaClientConfiguration implements Cloneable, SystemConfiguration {
    private int workerThreads = 4;
    private int connectTimeoutMillis = 3000;
    private int channelMaxIdleTimeSeconds = 120;
    private int socketSndBufSize = Integer.getInteger(IO_REMOTING_SOCKET_SNDBUF_SIZE, 65535);
    private int socketRcvBufSize = Integer.getInteger(IO_REMOTING_SOCKET_RCVBUF_SIZE, 65535);
    private int defaultExecutorThreads = 4;

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getChannelMaxIdleTimeSeconds() {
        return channelMaxIdleTimeSeconds;
    }

    public void setChannelMaxIdleTimeSeconds(int channelMaxIdleTimeSeconds) {
        this.channelMaxIdleTimeSeconds = channelMaxIdleTimeSeconds;
    }

    public int getSocketSndBufSize() {
        return socketSndBufSize;
    }

    public void setSocketSndBufSize(int socketSndBufSize) {
        this.socketSndBufSize = socketSndBufSize;
    }

    public int getSocketRcvBufSize() {
        return socketRcvBufSize;
    }

    public void setSocketRcvBufSize(int socketRcvBufSize) {
        this.socketRcvBufSize = socketRcvBufSize;
    }

    public int getDefaultExecutorThreads() {
        return defaultExecutorThreads;
    }

    public void setDefaultExecutorThreads(int defaultExecutorThreads) {
        this.defaultExecutorThreads = defaultExecutorThreads;
    }

    @Override
    protected MinaClientConfiguration clone() throws CloneNotSupportedException {
        return (MinaClientConfiguration) super.clone();
    }
}
