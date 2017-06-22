package io.lnk.api.broker;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.lnk.api.SystemConfiguration;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月19日 下午10:28:53
 */
public class BrokerConfiguration implements Cloneable, SystemConfiguration {
    private BrokerProvider provider = BrokerProvider.HTTP;
    private int listenPort = 9999;
    private int workerThreads = 10;
    private int selectorThreads = 5;
    private int channelMaxIdleTimeSeconds = 120;
    private int socketSndBufSize = Integer.getInteger(IO_SOCKET_SNDBUF_SIZE, 65535);
    private int socketRcvBufSize = Integer.getInteger(IO_SOCKET_RCVBUF_SIZE, 65535);
    private boolean pooledByteBufAllocatorEnable = true;
    private int defaultWorkerProcessorThreads = 10;
    private int defaultExecutorThreads = 8;
    private String context;
    
    /**
     * make make install
     * ../glibc-2.10.1/configure \ --prefix=/usr \ --with-headers=/usr/include \
     * --host=x86_64-linux-gnu \ --build=x86_64-pc-linux-gnu \ --without-gd
     */
    private boolean useEpollNativeSelector = false;

    public BrokerProvider getProvider() {
        return provider;
    }

    public void setProvider(BrokerProvider provider) {
        this.provider = provider;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getSelectorThreads() {
        return selectorThreads;
    }

    public void setSelectorThreads(int selectorThreads) {
        this.selectorThreads = selectorThreads;
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

    public boolean isPooledByteBufAllocatorEnable() {
        return pooledByteBufAllocatorEnable;
    }

    public void setPooledByteBufAllocatorEnable(boolean pooledByteBufAllocatorEnable) {
        this.pooledByteBufAllocatorEnable = pooledByteBufAllocatorEnable;
    }

    public int getDefaultWorkerProcessorThreads() {
        return defaultWorkerProcessorThreads;
    }

    public void setDefaultWorkerProcessorThreads(int defaultWorkerProcessorThreads) {
        this.defaultWorkerProcessorThreads = defaultWorkerProcessorThreads;
    }

    public int getDefaultExecutorThreads() {
        return defaultExecutorThreads;
    }

    public void setDefaultExecutorThreads(int defaultExecutorThreads) {
        this.defaultExecutorThreads = defaultExecutorThreads;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public boolean isUseEpollNativeSelector() {
        return useEpollNativeSelector;
    }

    public void setUseEpollNativeSelector(boolean useEpollNativeSelector) {
        this.useEpollNativeSelector = useEpollNativeSelector;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return (BrokerConfiguration) super.clone();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
