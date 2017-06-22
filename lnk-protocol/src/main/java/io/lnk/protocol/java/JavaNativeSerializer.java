package io.lnk.protocol.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.codec.binary.Base64;

import io.lnk.api.protocol.Serializer;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 上午11:54:19
 */
@SuppressWarnings("unchecked")
public class JavaNativeSerializer implements Serializer {

    @Override
    public String serializeAsString(Object bean) {
        byte[] serializeAsBytes = this.serializeAsBytes(bean);
        return Base64.encodeBase64String(serializeAsBytes);
    }

    @Override
    public byte[] serializeAsBytes(Object bean) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(bean);
            oout.close();
            return bout.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, String serializeString) {
        byte[] serializeAsBytes = Base64.decodeBase64(serializeString);
        return this.deserialize(clazz, serializeAsBytes);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] serializeBytes) {
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(serializeBytes);
            ObjectInputStream oin = new ObjectInputStream(bin);
            return (T) oin.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
