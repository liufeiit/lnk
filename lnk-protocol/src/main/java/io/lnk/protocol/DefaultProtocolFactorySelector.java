package io.lnk.protocol;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.protocol.hessian.HessianProtocolFactory;
import io.lnk.protocol.jackson.JacksonProtocolFactory;
import io.lnk.protocol.java.JavaNativeProtocolFactory;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午5:53:37
 */
public class DefaultProtocolFactorySelector implements ProtocolFactorySelector {
    private static final Logger log = LoggerFactory.getLogger(DefaultProtocolFactorySelector.class.getSimpleName());
    private ConcurrentHashMap<Integer, ProtocolFactory> protocolFactories = new ConcurrentHashMap<Integer, ProtocolFactory>();

    public DefaultProtocolFactorySelector() {
        super();
        this.registry(new JacksonProtocolFactory());
        this.registry(new JavaNativeProtocolFactory());
        this.registry(new HessianProtocolFactory());
    }

    @Override
    public ProtocolFactory select(int protocol) {
        ProtocolFactory protocolFactory = protocolFactories.get(protocol);
        return protocolFactory;
    }

    public ProtocolFactory registry(ProtocolFactory protocolFactory) {
        ProtocolFactory pf = protocolFactories.putIfAbsent(protocolFactory.getProtocol(), protocolFactory);
        log.info("registry protocolFactory code : {}, protocolFactory : {}, putIfAbsent : {}", protocolFactory.getProtocol(), protocolFactory, pf);
        return pf;
    }

    public void setProtocolFactories(List<ProtocolFactory> protocolFactories) {
        if (CollectionUtils.isEmpty(protocolFactories)) {
            return;
        }
        for (ProtocolFactory protocolFactory : protocolFactories) {
            ProtocolFactory pf = this.protocolFactories.putIfAbsent(protocolFactory.getProtocol(), protocolFactory);
            log.info("registry protocolFactory code : {}, protocolFactory : {}, putIfAbsent : {}", protocolFactory.getProtocol(), protocolFactory, pf);
        }
    }
}
