package io.lnk.demo.sync_multi_version_ploy;

import com.alibaba.fastjson.annotation.JSONField;

import io.lnk.demo.AuthResponse;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月4日 上午12:22:00
 */
public class PolyAuthResponse extends AuthResponse {
    private static final long serialVersionUID = -6167016575438427499L;
    private String principalId;
    private String txnType;
    private String productType;
    private String idType;
    @JSONField(serialize = false)
    private byte[] data;
    public byte[] getData() {
        return data;
    }
    public void setData(byte[] data) {
        this.data = data;
    }
    public String getPrincipalId() {
        return principalId;
    }
    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }
    public String getTxnType() {
        return txnType;
    }
    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }
    public String getProductType() {
        return productType;
    }
    public void setProductType(String productType) {
        this.productType = productType;
    }
    public String getIdType() {
        return idType;
    }
    public void setIdType(String idType) {
        this.idType = idType;
    }
    @Override
    public String toString() {
        return "PolyAuthResponse [principalId=" + principalId + ", txnType=" + txnType + ", productType=" + productType + ", idType=" + idType + "] " + super.toString();
    }
}
