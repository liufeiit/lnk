package io.lnk.protocol.broker;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.api.protocol.broker.BrokerProtocolFactory;
import io.lnk.api.protocol.broker.BrokerProtocolFactorySelector;
import io.lnk.protocol.broker.jackson.JacksonBrokerProtocolFactory;
import io.lnk.protocol.broker.xml.xstream.XStreamBrokerProtocolFactory;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 下午6:08:56
 */
public class LnkBrokerProtocolFactorySelector implements BrokerProtocolFactorySelector {
    private static final Logger log = LoggerFactory.getLogger(LnkBrokerProtocolFactorySelector.class.getSimpleName());
    private ConcurrentHashMap<String, BrokerProtocolFactory> brokerProtocolFactories = new ConcurrentHashMap<String, BrokerProtocolFactory>();

    public LnkBrokerProtocolFactorySelector() {
        super();
        this.registry(new JacksonBrokerProtocolFactory());
        this.registry(new XStreamBrokerProtocolFactory());
    }

    @Override
    public BrokerProtocolFactory select(String protocol) {
        BrokerProtocolFactory brokerProtocolFactory = this.brokerProtocolFactories.get(protocol);
        return brokerProtocolFactory;
    }
    
    public BrokerProtocolFactory registry(BrokerProtocolFactory brokerProtocolFactory) {
        BrokerProtocolFactory pf = brokerProtocolFactories.putIfAbsent(brokerProtocolFactory.getProtocol(), brokerProtocolFactory);
        log.info("registry BrokerProtocolFactory code : {}, brokerProtocolFactory : {}, putIfAbsent : {}", brokerProtocolFactory.getProtocol(), brokerProtocolFactory, pf);
        return pf;
    }

    public void setBrokerProtocolFactories(List<BrokerProtocolFactory> brokerProtocolFactories) {
        if (CollectionUtils.isEmpty(brokerProtocolFactories)) {
            return;
        }
        for (BrokerProtocolFactory brokerProtocolFactory : brokerProtocolFactories) {
            BrokerProtocolFactory pf = this.brokerProtocolFactories.putIfAbsent(brokerProtocolFactory.getProtocol(), brokerProtocolFactory);
            log.info("registry BrokerProtocolFactory code : {}, brokerProtocolFactory : {}, putIfAbsent : {}", brokerProtocolFactory.getProtocol(), brokerProtocolFactory, pf);
        }
    }
}
