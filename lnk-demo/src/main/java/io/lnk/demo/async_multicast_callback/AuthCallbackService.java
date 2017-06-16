package io.lnk.demo.async_multicast_callback;

import java.util.concurrent.CountDownLatch;

import io.lnk.api.InvokeType;
import io.lnk.api.annotation.LnkMethod;
import io.lnk.api.annotation.LnkService;
import io.lnk.demo.AppBizException;
import io.lnk.demo.AuthResponse;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 下午10:00:19
 */
@LnkService(group = "biz-pay-bgw-payment.callback.srv")
public interface AuthCallbackService {
    CountDownLatch sync = new CountDownLatch(1);
    CountDownLatch sync2 = new CountDownLatch(2);
    @LnkMethod(type = InvokeType.ASYNC)
    void callback(AuthResponse response) throws AppBizException;
}
