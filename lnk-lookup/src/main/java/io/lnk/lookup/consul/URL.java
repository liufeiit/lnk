package io.lnk.lookup.consul;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Objects;

public class URL {
    private String host;
    private int port;
    private String serviceId;
    private String serviceGroup;
    private int protocol;
    private String version;
    private Map<String, String> parameters;

    public URL(String host, int port) {
        this(null, null, null, 0, host, port);
    }

    public URL(String serviceGroup, String serviceId, String version, int protocol, String host, int port) {
        this(serviceGroup, serviceId, version, protocol, host, port, new HashMap<String, String>());
    }

    public URL(String serviceGroup, String serviceId, String version, int protocol, String host, int port, Map<String, String> parameters) {
        this.serviceGroup = serviceGroup;
        this.serviceId = serviceId;
        this.version = version;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.parameters = parameters;
    }

    public String getSimpleStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(serviceGroup).append("/").append(serviceId).append("/").append(version).append("/").append(protocol);
        return sb.toString();
    }

    public static String[] toAddressArray(List<URL> urlList) {
        if (urlList != null && urlList.size() > 0) {
            String[] ret = new String[urlList.size()];
            for (int i = 0; i < urlList.size(); i++) {
                ret[i] = urlList.get(i).getHost() + ":" + urlList.get(i).getPort();
            }
            return ret;
        } else {
            return new String[0];
        }
    }

    public String toFullStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(serviceGroup).append("/").append(serviceId).append("/").append(version).append("/").append(protocol).append("/").append(host).append("/").append(port);
        return sb.toString();
    }

    public static URL valueOf(String url) {
        if (StringUtils.isBlank(url)) {
            throw new ConsulException("url is null");
        }
        String[] value = url.split("/");
        if (value.length != 6) {
            throw new ConsulException("url is null");
        }
        return new URL(value[0], value[1], value[2], Integer.parseInt(value[3]), value[4], Integer.parseInt(value[5]));
    }

    private static String buildHostPortStr(String host, int defaultPort) {
        if (defaultPort <= 0) {
            return host;
        }
        int idx = host.indexOf(":");
        if (idx < 0) {
            return host + ":" + defaultPort;
        }
        int port = Integer.parseInt(host.substring(idx + 1));
        if (port <= 0) {
            return host.substring(0, idx + 1) + defaultPort;
        }
        return host;
    }

    public URL createCopy() {
        Map<String, String> params = new HashMap<String, String>();
        if (this.parameters != null) {
            params.putAll(this.parameters);
        }
        return new URL(serviceGroup, serviceId, version, protocol, host, port, params);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public String getParameter(String name, String defaultValue) {
        String value = getParameter(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public void addParameter(String name, String value) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            return;
        }
        parameters.put(name, value);
    }

    public void removeParameter(String name) {
        if (name != null) {
            parameters.remove(name);
        }
    }

    public void addParameters(Map<String, String> params) {
        parameters.putAll(params);
    }

    public void addParameterIfAbsent(String name, String value) {
        if (hasParameter(name)) {
            return;
        }
        parameters.put(name, value);
    }

    public Boolean getBooleanParameter(String name, boolean defaultValue) {
        String value = getParameter(name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public Boolean getBooleanParameter(String name) {
        String value = parameters.get(name);
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    public String getUri() {
        StringBuilder sb = new StringBuilder("/");
        sb.append(serviceGroup).append("/").append(serviceId).append("/").append(version).append("/").append(protocol);
        return sb.toString();
    }

    public String getIdentity() {
        return getUri();
    }

    public String toString() {
        return getUri() + "/" + this.host + "/" + this.port;
    }

    public boolean hasParameter(String key) {
        return StringUtils.isNotBlank(getParameter(key));
    }

    public String getServerPortStr() {
        return buildHostPortStr(host, port);
    }

    @Override
    public int hashCode() {
        int factor = 31;
        int rs = 1;
        rs = factor * rs + Objects.hashCode(protocol);
        rs = factor * rs + Objects.hashCode(host == null ? "" : host);
        rs = factor * rs + Objects.hashCode(port);
        rs = factor * rs + Objects.hashCode(serviceId);
        rs = factor * rs + Objects.hashCode(serviceGroup);
        rs = factor * rs + Objects.hashCode(version);
        return rs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof URL)) {
            return false;
        }
        URL ou = (URL) obj;
        if (!Objects.equal(this.protocol, ou.protocol)) {
            return false;
        }
        if (!Objects.equal(this.host, ou.host)) {
            return false;
        }
        if (!Objects.equal(this.port, ou.port)) {
            return false;
        }
        if (!Objects.equal(this.version, ou.version)) {
            return false;
        }
        if (!Objects.equal(this.serviceId, ou.serviceId)) {
            return false;
        }
        return Objects.equal(this.serviceGroup, ou.serviceGroup);
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public int getProtocol() {
        return protocol;
    }
}
