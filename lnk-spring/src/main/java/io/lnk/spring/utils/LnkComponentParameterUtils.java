package io.lnk.spring.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月2日 下午2:13:26
 */
public class LnkComponentParameterUtils {
    private static final Logger log = LoggerFactory.getLogger(LnkComponentParameterUtils.class.getSimpleName());

    public static Map<String, String> parse(final Element element) {
        Map<String, String> parameters = new HashMap<String, String>();
        List<Element> parameterElements = DomUtils.getChildElementsByTagName(element, "parameter");
        if (CollectionUtils.isEmpty(parameterElements)) {
            return parameters;
        }
        for (Element parameterElement : parameterElements) {
            String name = StringUtils.defaultString(parameterElement.getAttribute("name"));
            String value = StringUtils.defaultString(parameterElement.getAttribute("value"));
            if (StringUtils.isBlank(name) || StringUtils.isBlank(value)) {
                continue;
            }
            parameters.put(name, value);
        }
        return parameters;
    }
    
    public static void wiredParameters(final Element element, Object bean) {
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        Map<String, String> parameters = LnkComponentParameterUtils.parse(element);
        if (MapUtils.isNotEmpty(parameters)) {
            for (Map.Entry<String, String> e : parameters.entrySet()) {
                String name = e.getKey();
                String value = e.getValue();
                try {
                    if (beanWrapper.isWritableProperty(name)) {
                        beanWrapper.setPropertyValue(name, value);
                    }
                } catch (Throwable ex) {
                    log.warn("write {} parameter : {} Error.", element.getNodeName(), name);
                }
            }
        }
    }
}
