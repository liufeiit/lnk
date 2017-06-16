package io.lnk.spring.core;

import org.springframework.context.ApplicationEvent;

import io.lnk.api.app.Application;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 上午11:03:26
 */
public class LnkApplicationEvent extends ApplicationEvent {
    private static final long serialVersionUID = -4664140119704222671L;

    public LnkApplicationEvent(Application application) {
        super(application);
    }
    
    public Application getApplication() {
        return (Application) super.getSource();
    }
}
