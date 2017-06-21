package io.lnk.api.protocol.broker;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 下午6:07:32
 */
public interface BrokerProtocolFactorySelector {
    BrokerProtocolFactory select(String protocol);
}
