package io.lnk.demo.async_multicast_callback;

import io.lnk.api.InvokeType;
import io.lnk.api.annotation.LnkMethod;
import io.lnk.api.annotation.LnkService;
import io.lnk.demo.AppBizException;
import io.lnk.demo.AuthRequest;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 下午10:00:19
 */
@LnkService(group = "biz-pay-bgw-payment.srv")
public interface AuthService {
    @LnkMethod(type = InvokeType.ASYNC)
    void auth(AuthRequest request, AuthCallbackService callback) throws AppBizException;
    @LnkMethod(type = InvokeType.ASYNC_MULTI_CAST)
    void auth_multi_cast(AuthRequest request, AuthCallbackService callback) throws AppBizException;
}
