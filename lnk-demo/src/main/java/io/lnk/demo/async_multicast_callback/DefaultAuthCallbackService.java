package io.lnk.demo.async_multicast_callback;

import com.alibaba.fastjson.JSON;

import io.lnk.demo.AppBizException;
import io.lnk.demo.AuthResponse;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 下午11:54:59
 */
public class DefaultAuthCallbackService implements AuthCallbackService {

    @Override
    public void callback(AuthResponse response) throws AppBizException {
        System.err.println("resv response : " + JSON.toJSONString(response, true));
        sync.countDown();
        sync2.countDown();
    }
}
