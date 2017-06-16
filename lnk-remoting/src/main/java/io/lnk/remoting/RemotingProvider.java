package io.lnk.remoting;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月16日 下午2:39:59
 */
public enum RemotingProvider {
    Netty("netty"), Mina("mina");
    
    private final String name;

    private RemotingProvider(String name) {
        this.name = name;
    }

    public static RemotingProvider valueOfProvider(String name) {
        for (RemotingProvider remotingProvider : values()) {
            if (StringUtils.equalsIgnoreCase(remotingProvider.name, name)) {
                return remotingProvider;
            }
        }
        return Netty;
    }
}
