package io.lnk.protocol.xml.xstream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import io.lnk.protocol.Serializer;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 下午6:45:15
 */
public class XStreamSerializer implements Serializer {

    @Override
    public String serializeAsString(Object bean) {
        XStream xstream = new XStream();
        xstream.processAnnotations(bean.getClass());
        xstream.registerConverter(new DateConverter());
        return xstream.toXML(bean);
    }

    @Override
    public byte[] serializeAsBytes(Object bean) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XStream xstream = new XStream();
        xstream.processAnnotations(bean.getClass());
        xstream.registerConverter(new DateConverter());
        xstream.toXML(bean, out);
        return out.toByteArray();
    }

    @Override
    public <T> T deserialize(Class<T> clazz, String serializeString) {
        XStream xstream = new XStream(new DomDriver());
        xstream.processAnnotations(clazz);
        xstream.registerConverter(new BlankFilterConverter());
        xstream.registerConverter(new IntegerFilterConverter());
        xstream.registerConverter(new LongFilterConverter());
        xstream.registerConverter(new DateConverter());
        Object object = xstream.fromXML(serializeString);
        return clazz.cast(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] serializeBytes) {
        XStream xstream = new XStream(new DomDriver());
        xstream.processAnnotations(clazz);
        xstream.registerConverter(new BlankFilterConverter());
        xstream.registerConverter(new IntegerFilterConverter());
        xstream.registerConverter(new LongFilterConverter());
        xstream.registerConverter(new DateConverter());
        Object object = xstream.fromXML(new ByteArrayInputStream(serializeBytes));
        return clazz.cast(object);
    }
}
