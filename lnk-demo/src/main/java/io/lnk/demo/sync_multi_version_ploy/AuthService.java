package io.lnk.demo.sync_multi_version_ploy;

import io.lnk.api.InvokeType;
import io.lnk.api.annotation.LnkMethod;
import io.lnk.api.annotation.LnkService;
import io.lnk.api.exception.AppBizException;
import io.lnk.demo.AuthRequest;
import io.lnk.demo.AuthResponse;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 下午10:00:19
 */
@LnkService(group = "biz-pay-bgw-payment.srv")
public interface AuthService {
    @LnkMethod(type = InvokeType.SYNC, timeoutMillis = 3000L)
    AuthResponse auth(AuthRequest request) throws AppBizException;
    
    @LnkMethod(type = InvokeType.SYNC, timeoutMillis = 600000L)
    AuthResponse auth_poly(AuthRequest request) throws AppBizException;
}
