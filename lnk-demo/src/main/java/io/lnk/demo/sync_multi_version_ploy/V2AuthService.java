package io.lnk.demo.sync_multi_version_ploy;

import io.lnk.api.annotation.LnkVersion;
import io.lnk.api.exception.AppBizException;
import io.lnk.demo.AuthRequest;
import io.lnk.demo.AuthResponse;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 下午10:10:43
 */
@LnkVersion("2.0.0")
public class V2AuthService implements AuthService {

    @Override
    public AuthResponse auth(AuthRequest request) throws AppBizException {
        AuthResponse response = new AuthResponse();
        response.setGateId("0312");
        response.setGateRespCode("TXN.000");
        response.setGateRespMsg("V2 : " + request);
        response.setTxnId(request.getTxnId());
        return response;
    }

    @Override
    public AuthResponse auth_poly(AuthRequest request) throws AppBizException {
        PolyAuthResponse response = new PolyAuthResponse();
        response.setGateId("0104");
        response.setGateRespCode("TXN.000");
        response.setGateRespMsg("V2 : " + request);
        response.setTxnId(request.getTxnId());
        
        response.setPrincipalId("101");
        response.setTxnType("00001");
        response.setProductType("QP");
        response.setIdType("01");
        return response;
    }
}
