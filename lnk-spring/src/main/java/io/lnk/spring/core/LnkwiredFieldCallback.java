package io.lnk.spring.core;

import java.lang.reflect.Field;

import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.ReflectionUtils.FieldCallback;

import io.lnk.api.RemoteObjectFactory;
import io.lnk.api.RemoteObjectFactoryAware;
import io.lnk.api.annotation.Lnkwired;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月12日 下午3:57:12
 */
public class LnkwiredFieldCallback implements FieldCallback {
    private final Object bean;
    private final RemoteObjectFactory remoteObjectFactory;

    public LnkwiredFieldCallback(RemoteObjectFactory remoteObjectFactory, Object bean) {
        super();
        this.remoteObjectFactory = remoteObjectFactory;
        this.bean = bean;
    }

    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        if (bean instanceof RemoteObjectFactoryAware) {
            if (field.getType().equals(RemoteObjectFactory.class)) {
                return;
            }
        }
        ConfigurablePropertyAccessor configurablePropertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(bean);
        Object fieldValue = configurablePropertyAccessor.getPropertyValue(field.getName());
        if (fieldValue != null) {
            return;
        }
        Lnkwired lnkwired = field.getAnnotation(Lnkwired.class);
        configurablePropertyAccessor.setPropertyValue(field.getName(), remoteObjectFactory.getServiceObject(field.getType(), lnkwired.version()));
    }
}
