package io.lnk.api.broker;

import io.lnk.api.exception.LnkException;
import io.lnk.api.exception.LnkTimeoutException;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 上午11:16:27
 */
public interface BrokerCaller {
    BrokerCommand sync(final BrokerCommand command) throws LnkException, LnkTimeoutException;
    void async(final BrokerCommand command) throws LnkException, LnkTimeoutException;
    void async_multicast(final BrokerCommand command);
}
