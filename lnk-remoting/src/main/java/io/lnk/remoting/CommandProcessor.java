package io.lnk.remoting;

import io.lnk.remoting.protocol.RemotingCommand;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月19日 下午9:14:07
 */
public interface CommandProcessor {
    RemotingCommand processCommand(RemotingCommand request) throws Throwable;
    boolean tryAcquireFailure(long timeoutMillis);
    void release();
}
