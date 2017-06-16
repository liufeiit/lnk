package io.lnk.demo;

import io.lnk.api.RemoteObjectFactory;
import io.lnk.api.RemoteObjectFactoryAware;
import io.lnk.api.annotation.Lnkwired;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月25日 下午7:10:35
 */
public class BasicService implements RemoteObjectFactoryAware {

    @Lnkwired(version = "2.0.0")
    protected HelloService helloService;
    
    @Lnkwired
    protected DemoService demoService;

    protected RemoteObjectFactory remoteObjectFactory;
    
    @Override
    public void setRemoteObjectFactory(RemoteObjectFactory remoteObjectFactory) {
        this.remoteObjectFactory = remoteObjectFactory;
    }
}
