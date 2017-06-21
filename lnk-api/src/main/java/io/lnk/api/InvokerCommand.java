package io.lnk.api;

import java.io.Serializable;

import io.lnk.api.app.Application;
import io.lnk.api.exception.transport.CommandTransportException;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午12:56:45
 */
public class InvokerCommand implements Serializable {
    private static final long serialVersionUID = 7227421307748173734L;
    private String id;
    private String ip;
    private Application application;
    private String version = ServiceVersion.DEFAULT_VERSION;
    private int protocol = Protocols.DEFAULT_PROTOCOL;
    private String serviceGroup;
    private String serviceId;
    private String method;
    private Class<?>[] signature;
    private ProtocolObject[] args;
    private ProtocolObject retObject;
    private CommandTransportException exception;

    public String commandSignature() {
        StringBuilder sb = new StringBuilder(serviceId).append(".").append(method).append("(");
        for (Class<?> signType : signature) {
            sb.append(signType.getName()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Class<?>[] getSignature() {
        return signature;
    }

    public void setSignature(Class<?>[] signature) {
        this.signature = signature;
    }

    public ProtocolObject[] getArgs() {
        return args;
    }

    public void setArgs(ProtocolObject[] args) {
        this.args = args;
    }

    public ProtocolObject getRetObject() {
        return retObject;
    }

    public void setRetObject(ProtocolObject retObject) {
        this.retObject = retObject;
    }

    public CommandTransportException getException() {
        return exception;
    }

    public void setException(CommandTransportException exception) {
        this.exception = exception;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
