package io.lnk.demo;

import java.io.Serializable;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月3日 下午10:00:46
 */
public class AuthRequest implements Serializable {
    private static final long serialVersionUID = 1064210897267842421L;
    private String txnId;
    private String memberId;
    private String name;
    private String mobile;
    private String cardNo;
    private String identityNo;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getIdentityNo() {
        return identityNo;
    }

    public void setIdentityNo(String identityNo) {
        this.identityNo = identityNo;
    }

    @Override
    public String toString() {
        return "AuthRequest[txnId=" + txnId + ", memberId=" + memberId + ", name=" + name + ", mobile=" + mobile + ", cardNo=" + cardNo + ", identityNo=" + identityNo + "]";
    }
}
