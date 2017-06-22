package io.lnk.api.protocol;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月18日 下午3:49:33
 */
public interface Serializer {

    String serializeAsString(Object bean);
    
    byte[] serializeAsBytes(Object bean);

    <T> T deserialize(Class<T> clazz, String serializeString);

    <T> T deserialize(Class<T> clazz, byte[] serializeBytes);
}
