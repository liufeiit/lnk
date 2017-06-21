package io.lnk.api.broker;

import java.io.Serializable;

import io.lnk.api.BrokerProtocols;
import io.lnk.api.Protocols;
import io.lnk.api.ServiceVersion;
import io.lnk.api.app.Application;

/**
 * 使用JSON协议代理调用.
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 上午11:22:07
 */
public class BrokerCommand implements Serializable {
    private static final long serialVersionUID = -133662870644828334L;
    private String id;// 可选
    private String ip;// 可选
    private Application application;
    private String version = ServiceVersion.DEFAULT_VERSION;
    private int protocol = Protocols.DEFAULT_PROTOCOL;
    private String brokerProtocol = BrokerProtocols.JACKSON;
    private String serviceGroup;
    private String serviceId;
    private String method;
    private String[] signature;
    private BrokerArg[] args;
    private String retObject;
    private String exception;
    private long timeoutMillis;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public String getBrokerProtocol() {
        return brokerProtocol;
    }

    public void setBrokerProtocol(String brokerProtocol) {
        this.brokerProtocol = brokerProtocol;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String[] getSignature() {
        return signature;
    }

    public void setSignature(String[] signature) {
        this.signature = signature;
    }

    public BrokerArg[] getArgs() {
        return args;
    }

    public void setArgs(BrokerArg[] args) {
        this.args = args;
    }

    public String getRetObject() {
        return retObject;
    }

    public void setRetObject(String retObject) {
        this.retObject = retObject;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }
}
