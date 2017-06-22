package io.lnk.protocol.hessian;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import io.lnk.api.protocol.Serializer;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 上午11:54:19
 */
@SuppressWarnings("unchecked")
public class HessianSerializer implements Serializer {

    @Override
    public byte[] serializeAsBytes(Object bean) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Hessian2Output hessian2Output = new Hessian2Output(bout);
            hessian2Output.writeObject(bean);
            hessian2Output.close();
            return bout.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] serializeBytes) {
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(serializeBytes);
            Hessian2Input hessian2Input = new Hessian2Input(bin);
            return (T) hessian2Input.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String serializeAsString(Object bean) {
        throw new RuntimeException("unsupport serializeAsString.");
    }

    @Override
    public <T> T deserialize(Class<T> clazz, String serializeString) {
        throw new RuntimeException("unsupport deserialize.");
    }
}
