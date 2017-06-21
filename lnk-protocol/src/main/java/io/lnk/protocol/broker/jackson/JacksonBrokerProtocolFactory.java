package io.lnk.protocol.broker.jackson;

import io.lnk.api.BrokerProtocols;
import io.lnk.protocol.broker.BasicBrokerProtocolFactory;
import io.lnk.protocol.jackson.JacksonSerializer;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 上午11:50:18
 */
public class JacksonBrokerProtocolFactory extends BasicBrokerProtocolFactory {

    public JacksonBrokerProtocolFactory() {
        super(BrokerProtocols.JACKSON, new JacksonSerializer());
    }
}
