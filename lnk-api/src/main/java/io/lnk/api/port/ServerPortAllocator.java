package io.lnk.api.port;

import io.lnk.api.app.Application;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午4:15:11
 */
public interface ServerPortAllocator {
    int selectPort(int expectListenPort, Application application);
}
