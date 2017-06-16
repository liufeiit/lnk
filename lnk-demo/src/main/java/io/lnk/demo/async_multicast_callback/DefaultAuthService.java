package io.lnk.demo.async_multicast_callback;

import io.lnk.demo.AppBizException;
import io.lnk.demo.AuthRequest;
import io.lnk.demo.AuthResponse;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 下午11:54:33
 */
public class DefaultAuthService implements AuthService {

    @Override
    public void auth(AuthRequest request, AuthCallbackService callback) throws AppBizException {
        AuthResponse response = new AuthResponse();
        response.setGateId("0101");
        response.setGateRespCode("TXN.000");
        response.setGateRespMsg("Default async callback : " + request);
        response.setTxnId(request.getTxnId());
        callback.callback(response);
    }

    @Override
    public void auth_multi_cast(AuthRequest request, AuthCallbackService callback) throws AppBizException {
        AuthResponse response = new AuthResponse();
        response.setGateId("0312");
        response.setGateRespCode("TXN.000");
        response.setGateRespMsg("Default async multicast callback : " + request);
        response.setTxnId(request.getTxnId());
        callback.callback(response);
    }
}
