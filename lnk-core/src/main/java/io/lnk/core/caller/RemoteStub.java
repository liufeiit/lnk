package io.lnk.core.caller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import io.lnk.api.ProtocolVersion;
import io.lnk.api.RemoteObject;
import io.lnk.api.ServiceVersion;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月23日 上午11:12:52
 */
public class RemoteStub implements RemoteObject {
    private static final Pattern REMOTE_STUB_PATTERN = Pattern.compile("^(.*),(.*),(.*),(.*)$");
    private String serviceId;
    private String serviceGroup;
    private String version = ServiceVersion.DEFAULT_VERSION;
    private int protocol = ProtocolVersion.DEFAULT_PROTOCOL;

    public RemoteStub() {
        super();
    }

    public RemoteStub(String serializeStub) {
        super();
        this.deserializeStub(serializeStub);
    }

    @Override
    public String serializeStub() {
        StringBuffer sb = new StringBuffer();
        sb.append(serviceId);
        sb.append(",").append(serviceGroup);
        sb.append(",").append(version);
        sb.append(",").append(protocol);
        return sb.toString();
    }

    private void deserializeStub(String serializeStub) {
        Matcher matcher = REMOTE_STUB_PATTERN.matcher(serializeStub);
        boolean matchFound = matcher.find();
        if (matchFound) {
            this.serviceId = StringUtils.defaultString(matcher.group(1));
            this.serviceGroup = StringUtils.defaultString(matcher.group(2));
            this.version = StringUtils.defaultString(matcher.group(3), ServiceVersion.DEFAULT_VERSION);
            this.protocol = NumberUtils.toInt(matcher.group(4), ProtocolVersion.DEFAULT_PROTOCOL);
            return;
        }
        throw new RuntimeException("Illegal serialize data for RemoteObject.");
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

    @Override
    public String toString() {
        return serializeStub();
    }
}
