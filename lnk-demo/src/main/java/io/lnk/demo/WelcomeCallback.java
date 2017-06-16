package io.lnk.demo;

import io.lnk.api.annotation.LnkService;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月27日 下午7:21:46
 */
@LnkService(group = "biz-pay-bgw-payment.srv")
public interface WelcomeCallback {
    void callback(String arg);
}
