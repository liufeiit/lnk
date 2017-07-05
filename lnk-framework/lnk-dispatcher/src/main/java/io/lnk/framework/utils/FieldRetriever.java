package io.lnk.framework.utils;

import java.lang.reflect.Field;

import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月1日 下午11:11:04
 */
public class FieldRetriever {

    private static ClassLoader classLoader;

    static {
        classLoader = FieldRetriever.class.getClassLoader();
    }

    public static <T> T getObject(String field, Class<T> clazz) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        int lastDotIndex = field.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == field.length()) {
            throw new IllegalArgumentException("staticField must be a fully qualified class plus static field name.");
        }
        String className = field.substring(0, lastDotIndex);
        String fieldName = field.substring(lastDotIndex + 1);
        Class<?> targetClass = ClassUtils.forName(className, classLoader);
        String targetField = fieldName;
        Field fieldObject = targetClass.getField(targetField);
        ReflectionUtils.makeAccessible(fieldObject);
        return clazz.cast(fieldObject.get(null));
    }

    public static Object getObject(String field) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        int lastDotIndex = field.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == field.length()) {
            throw new IllegalArgumentException("staticField must be a fully qualified class plus static field name.");
        }
        String className = field.substring(0, lastDotIndex);
        String fieldName = field.substring(lastDotIndex + 1);
        Class<?> targetClass = ClassUtils.forName(className, classLoader);
        String targetField = fieldName;
        Field fieldObject = targetClass.getField(targetField);
        ReflectionUtils.makeAccessible(fieldObject);
        return fieldObject.get(null);
    }

    public static <T> T getObject(String field, Object targetObject, Class<T> clazz) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        int lastDotIndex = field.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == field.length()) {
            throw new IllegalArgumentException("staticField must be a fully qualified class plus static field name.");
        }
        String className = field.substring(0, lastDotIndex);
        String fieldName = field.substring(lastDotIndex + 1);
        Class<?> targetClass = ClassUtils.forName(className, classLoader);
        String targetField = fieldName;
        Field fieldObject = targetClass.getField(targetField);
        ReflectionUtils.makeAccessible(fieldObject);
        return clazz.cast(fieldObject.get(targetObject));
    }

    public static Object getObject(String field, Object targetObject) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        int lastDotIndex = field.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == field.length()) {
            throw new IllegalArgumentException("staticField must be a fully qualified class plus static field name.");
        }
        String className = field.substring(0, lastDotIndex);
        String fieldName = field.substring(lastDotIndex + 1);
        Class<?> targetClass = ClassUtils.forName(className, classLoader);
        String targetField = fieldName;
        Field fieldObject = targetClass.getField(targetField);
        ReflectionUtils.makeAccessible(fieldObject);
        return fieldObject.get(targetObject);
    }
}
