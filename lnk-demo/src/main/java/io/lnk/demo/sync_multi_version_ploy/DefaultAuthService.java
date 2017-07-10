package io.lnk.demo.sync_multi_version_ploy;

import io.lnk.api.annotation.LnkVersion;
import io.lnk.api.exception.AppBizException;
import io.lnk.demo.AuthRequest;
import io.lnk.demo.AuthResponse;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 下午10:10:07
 */
@LnkVersion
public class DefaultAuthService implements AuthService {

    @Override
    public AuthResponse auth(AuthRequest request) throws AppBizException {
        if (request.getName().equals("异常")) {
            throw new AppBizException("EX.001", "测试一下异常 : " + request);
        }
        AuthResponse response = new AuthResponse();
        response.setGateId("0101");
        response.setGateRespCode("TXN.000");
        response.setGateRespMsg("Default V1 : " + request);
        response.setTxnId(request.getTxnId());
        return response;
    }

    @Override
    public AuthResponse auth_poly(AuthRequest request) throws AppBizException {
        PolyAuthRequest polyAuthRequest = (PolyAuthRequest) request;
        PolyAuthResponse response = new PolyAuthResponse();
        response.setGateId("0106");
        response.setGateRespCode("TXN.000");
        response.setGateRespMsg("Default V1 : " + polyAuthRequest);
        response.setTxnId(polyAuthRequest.getTxnId());
        response.setData(polyAuthRequest.getData());
        response.setPrincipalId("101");
        response.setTxnType("00001");
        response.setProductType("QP");
        response.setIdType("01");
        return response;
    }

}
