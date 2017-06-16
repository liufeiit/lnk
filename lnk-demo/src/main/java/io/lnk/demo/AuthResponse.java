package io.lnk.demo;

import java.io.Serializable;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 下午10:03:48
 */
public class AuthResponse implements Serializable {
    private static final long serialVersionUID = -4355447998900266738L;
    private String txnId;
    private String gateId;
    private String gateRespCode;
    private String gateRespMsg;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getGateId() {
        return gateId;
    }

    public void setGateId(String gateId) {
        this.gateId = gateId;
    }

    public String getGateRespCode() {
        return gateRespCode;
    }

    public void setGateRespCode(String gateRespCode) {
        this.gateRespCode = gateRespCode;
    }

    public String getGateRespMsg() {
        return gateRespMsg;
    }

    public void setGateRespMsg(String gateRespMsg) {
        this.gateRespMsg = gateRespMsg;
    }
}
