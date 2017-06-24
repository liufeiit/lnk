package io.lnk.api.broker;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月16日 下午2:39:59
 */
public enum BrokerProtocol {
    HTTP("http"), WS("ws");
    
    private final String name;

    private BrokerProtocol(String name) {
        this.name = name;
    }

    public static BrokerProtocol valueOfProtocol(String name) {
        for (BrokerProtocol brokerProtocol : values()) {
            if (StringUtils.equalsIgnoreCase(brokerProtocol.name, name)) {
                return brokerProtocol;
            }
        }
        return HTTP;
    }
}
