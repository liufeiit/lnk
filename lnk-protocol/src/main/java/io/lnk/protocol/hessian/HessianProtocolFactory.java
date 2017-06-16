package io.lnk.protocol.hessian;

import io.lnk.api.ProtocolVersion;
import io.lnk.protocol.BasicProtocolFactory;
import io.lnk.protocol.Serializer;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 上午11:44:15
 */
public class HessianProtocolFactory extends BasicProtocolFactory {
    private final Serializer serializer;

    public HessianProtocolFactory() {
        super(ProtocolVersion.HESSIAN_PROTOCOL);
        this.serializer = new HessianSerializer();
    }

    @Override
    public byte[] encode(Object obj) {
        return this.serializer.serializeAsBytes(obj);
    }

    @Override
    public <T> T decode(Class<T> objType, byte[] data) {
        return this.serializer.deserialize(objType, data);
    }
}
