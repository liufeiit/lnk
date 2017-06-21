package io.lnk.api.broker;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 下午1:31:17
 */
public interface BrokerCallerAware {
    void setBrokerCaller(BrokerCaller caller);
}
