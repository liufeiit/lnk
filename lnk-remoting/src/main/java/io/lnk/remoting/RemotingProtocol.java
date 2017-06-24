package io.lnk.remoting;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月16日 下午2:39:59
 */
public enum RemotingProtocol {
    Netty("netty"), Mina("mina");
    
    private final String name;

    private RemotingProtocol(String name) {
        this.name = name;
    }

    public static RemotingProtocol valueOfProtocol(String name) {
        for (RemotingProtocol remotingProtocol : values()) {
            if (StringUtils.equalsIgnoreCase(remotingProtocol.name, name)) {
                return remotingProtocol;
            }
        }
        return Netty;
    }
}
