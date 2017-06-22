package io.lnk.api.broker;

import java.net.InetSocketAddress;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月22日 上午11:22:26
 */
public interface BrokerServer extends BrokerCallerAware {
    void start();
    void shutdown();
    InetSocketAddress getServerAddress();
    void setContext(String context);
}
