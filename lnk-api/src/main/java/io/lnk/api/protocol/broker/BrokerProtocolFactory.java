package io.lnk.api.protocol.broker;

import io.lnk.api.InvokerCommand;
import io.lnk.api.broker.BrokerCommand;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.protocol.object.ObjectProtocolFactory;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 上午11:45:28
 */
public interface BrokerProtocolFactory {
    String getProtocol();
    InvokerCommand encode(BrokerCommand command, ObjectProtocolFactory objectProtocolFactory, ProtocolFactory protocolFactory) throws Throwable;
    BrokerCommand decode(InvokerCommand command, ProtocolFactory protocolFactory) throws Throwable;
}
