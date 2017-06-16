package io.lnk.core;

import io.lnk.api.CommandArg;
import io.lnk.api.protocol.ProtocolFactory;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月30日 下午12:10:58
 */
public interface CommandArgProtocolFactory {
    CommandArg[] encode(Object[] args, ProtocolFactory protocolFactory) throws Throwable;
    Object[] decode(CommandArg[] args, ProtocolFactory protocolFactory) throws Throwable;
}
