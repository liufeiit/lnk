package io.lnk.demo;

import io.lnk.api.InvokeType;
import io.lnk.api.annotation.LnkMethod;
import io.lnk.api.annotation.LnkService;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午8:52:14
 */

@LnkService(group = "biz-pay-bgw-payment.router.srv")
public interface DemoService {
    @LnkMethod(type = InvokeType.SYNC, timeoutMillis = 5000L)
    String demo(String name);
}
