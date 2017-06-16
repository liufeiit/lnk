package io.lnk.protocol.jackson;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

import io.lnk.protocol.Serializer;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月18日 下午3:56:00
 */
public class JacksonSerializer implements Serializer {

    private final ObjectMapper objectMapper;
    
    public JacksonSerializer() {
        this(false, false);
    }

    public JacksonSerializer(boolean jacksonSmile) {
        this(jacksonSmile, false);
    }

    public JacksonSerializer(boolean jacksonSmile, boolean pretty) {
        this("yyyyMMddHHmmss", jacksonSmile, pretty);
    }

    public JacksonSerializer(String datePattern, boolean jacksonSmile, boolean pretty) {
        super();
        if (jacksonSmile) {
            objectMapper = new ObjectMapper(new SmileFactory());
        } else {
            objectMapper = new ObjectMapper();
        }
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.setDateFormat(new SimpleDateFormat(datePattern));
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, pretty);
        Hibernate4Module module = new Hibernate4Module();
        module.enable(Hibernate4Module.Feature.USE_TRANSIENT_ANNOTATION);
        objectMapper.registerModule(module);
    }

    public String serializeAsString(Object bean) {
        try {
            return objectMapper.writeValueAsString(bean);
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public byte[] serializeAsBytes(Object bean) {
        try {
            return objectMapper.writeValueAsBytes(bean);
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public <T> T deserialize(Class<T> clazz, String serializeString) {
        try {
            return (T) objectMapper.readValue(serializeString, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public <T> T deserialize(Class<T> clazz, byte[] serializeBytes) {
        try {
            return (T) objectMapper.readValue(serializeBytes, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
