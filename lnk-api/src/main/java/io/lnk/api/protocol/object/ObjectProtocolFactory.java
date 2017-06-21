package io.lnk.api.protocol.object;

import io.lnk.api.ProtocolObject;
import io.lnk.api.protocol.ProtocolFactory;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月30日 下午12:10:58
 */
public interface ObjectProtocolFactory {
    ProtocolObject[] encode(Object[] objs, ProtocolFactory protocolFactory) throws Throwable;
    Object[] decode(ProtocolObject[] objs, ProtocolFactory protocolFactory) throws Throwable;
}
