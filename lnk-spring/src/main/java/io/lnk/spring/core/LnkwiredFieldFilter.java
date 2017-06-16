package io.lnk.spring.core;

import java.lang.reflect.Field;

import org.springframework.util.ReflectionUtils.FieldFilter;

import io.lnk.api.annotation.Lnkwired;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月12日 下午2:38:38
 */
public class LnkwiredFieldFilter implements FieldFilter {
    public boolean matches(Field field) {
        return field.isAnnotationPresent(Lnkwired.class);
    }
}