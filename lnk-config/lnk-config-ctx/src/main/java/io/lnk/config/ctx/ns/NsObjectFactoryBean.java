package io.lnk.config.ctx.ns;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.jxpath.JXPathContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import io.lnk.config.ctx.utils.PropertyName;

/**
 * 名字服务对象工厂Bean类。
 */
public class NsObjectFactoryBean implements FactoryBean<Object>, InitializingBean, DisposableBean {
    /**
     * 对象类型
     */
    private Class<?> objectType;

    /**
     * 对象
     */
    private Object object;

    /**
     * 对象初始化方法
     */
    private String objectInitMethod;

    /**
     * 反射后的对象初始化方法
     */
    private Method reflectedObjectInitMethod;

    /**
     * 对象销毁方法
     */
    private String objectDestroyMethod;

    /**
     * 反射后的对象销毁方法
     */
    private Method reflectedObjectDestroyMethod;

    /**
     * 名字服务路径
     */
    private String nsPath;

    /**
     * 对象属性
     */
    private Map<String, Object> objectProperties;

    /**
     * 名字服务注册器
     */
    private NsRegistry nsRegistry;

    public Object getObject() throws Exception {
        Object bean = objectType.newInstance();
        nsRegistry.setProperties(nsPath, bean);

        if (objectProperties != null) {
            JXPathContext xpathContext = JXPathContext.newContext(bean);
            for (Entry<String, Object> entry : objectProperties.entrySet()) {
                xpathContext.setValue(PropertyName.unixStyleToJavaStyle(entry.getKey()), entry.getValue());
            }
        }

        if (reflectedObjectInitMethod != null) {
            reflectedObjectInitMethod.invoke(bean);
        }

        if (reflectedObjectDestroyMethod != null) {
            object = bean;
        }

        return bean;
    }

    public Class<?> getObjectType() {
        return objectType;
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() throws Exception {
        if (reflectedObjectDestroyMethod != null) {
            if (object != null) {
                reflectedObjectDestroyMethod.invoke(object);
            }
        }

        object = null;
    }

    public void afterPropertiesSet() throws Exception {
        if (objectInitMethod != null) {
            reflectedObjectInitMethod = objectType.getMethod(objectInitMethod);
        }
        if (objectDestroyMethod != null) {
            reflectedObjectDestroyMethod = objectType.getMethod(objectDestroyMethod);
        }
    }

    public Map<String, Object> getObjectProperties() {
        return objectProperties;
    }

    public void setObjectProperties(Map<String, Object> objectProperties) {
        this.objectProperties = objectProperties;
    }

    public String getObjectInitMethod() {
        return objectInitMethod;
    }

    public void setObjectInitMethod(String objectInitMethod) {
        this.objectInitMethod = objectInitMethod;
    }

    public String getObjectDestroyMethod() {
        return objectDestroyMethod;
    }

    public void setObjectDestroyMethod(String objectDestroyMethod) {
        this.objectDestroyMethod = objectDestroyMethod;
    }

    public String getNsPath() {
        return nsPath;
    }

    public void setNsPath(String nsPath) {
        this.nsPath = nsPath;
    }

    public NsRegistry getNsRegistry() {
        return nsRegistry;
    }

    @Autowired
    public void setNsRegistry(NsRegistry nsRegistry) {
        this.nsRegistry = nsRegistry;
    }

    public void setObjectType(Class<?> objectType) {
        this.objectType = objectType;
    }

}
