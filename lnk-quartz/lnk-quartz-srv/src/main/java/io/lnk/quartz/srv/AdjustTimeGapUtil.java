package io.lnk.quartz.srv;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.quartz.Trigger;

/**
 * 调整时间差工具类。
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午9:39:05
 */
public class AdjustTimeGapUtil {
    public static final Set<String> timeProperties = new HashSet<String>();

    static {
        timeProperties.add("startTime");
        timeProperties.add("endTime");
        timeProperties.add("nextFireTime");
        timeProperties.add("previousFireTime");
    }

    protected static void adjustTriggerImpl(Trigger trigger, long delta) throws Exception {
        BeanInfo info = java.beans.Introspector.getBeanInfo(trigger.getClass());
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (timeProperties.contains(pd.getName())) {
                Method rdMethod = pd.getReadMethod();
                Method wrMethod = pd.getWriteMethod();
                if (rdMethod == null || wrMethod == null) {
                    continue;
                }
                Date date1 = (Date) rdMethod.invoke(trigger);
                if (date1 == null) {
                    continue;
                }
                date1 = new java.util.Date(date1.getTime() + delta);
                wrMethod.invoke(trigger, date1);
            }
        }
    }

    public static void adjustTrigger(Trigger trigger, long delta) {
        if (delta == 0) {
            return;
        }
        try {
            adjustTriggerImpl(trigger, delta);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
