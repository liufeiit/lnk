package io.lnk.protocol.java;

import io.lnk.api.Protocols;
import io.lnk.protocol.BasicProtocolFactory;
import io.lnk.protocol.Serializer;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 上午11:44:15
 */
public class JavaNativeProtocolFactory extends BasicProtocolFactory {
    private final Serializer serializer;

    public JavaNativeProtocolFactory() {
        super(Protocols.JAVA_NATIVE_PROTOCOL);
        this.serializer = new JavaNativeSerializer();
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
