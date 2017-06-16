package io.lnk.api;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 形如：zk://root:lnk123@127.0.0.1:2181/lnk
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月2日 下午2:34:59
 */
public final class URI implements Serializable {
    private static final long serialVersionUID = 6607097074483173078L;
    private final String protocol;
    private final String username;
    private final String password;
    private final String host;
    private final int port;
    private final String path;
    private final Map<String, String> parameters;
    
    public URI(String protocol, String username, String password, String host, int port, String path, Map<String, String> parameters) {
        if ((username == null || username.length() == 0) && password != null && password.length() > 0) {
            throw new IllegalArgumentException("Invalid url, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        this.path = path;
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        } else {
            parameters = new HashMap<String, String>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }
    
    public static URI valueOf(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = null;
        int i = url.indexOf("?");
        if (i >= 0) {
            String[] parts = url.substring(i + 1).split("\\&");
            parameters = new HashMap<String, String>();
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            url = url.substring(0, i);
        }
        i = url.indexOf("://");
        if (i >= 0) {
            if (i == 0)
                throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else {
            // case: file:/path/to/file.txt
            i = url.indexOf(":/");
            if (i >= 0) {
                if (i == 0)
                    throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }
        i = url.indexOf("/");
        if (i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0, i);
        }
        i = url.indexOf("@");
        if (i >= 0) {
            username = url.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            url = url.substring(i + 1);
        }
        i = url.indexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }
        if (url.length() > 0) {
            host = url;
        }
        return new URI(protocol, username, password, host, port, path, parameters);
    }
    
    public Charset getCharset(String key, Charset charset) {
        String charsetName = this.getParameters().get(key);
        if (StringUtils.isNotBlank(charsetName)) {
            try {
                return Charset.forName(charsetName);
            } catch (Throwable e) {}
        }
        return charset;
    }
    
    public int getInt(String key, int defaultValue) {
        String intString = this.getParameters().get(key);
        if (StringUtils.isNotBlank(intString)) {
            return NumberUtils.toInt(intString, defaultValue);
        }
        return defaultValue;
    }
    
    public long getLong(String key, long defaultValue) {
        String valueString = this.getParameters().get(key);
        if (StringUtils.isNotBlank(valueString)) {
            return NumberUtils.toLong(valueString, defaultValue);
        }
        return defaultValue;
    }

    public URI addParameter(String key, String value) {
        if (key == null || key.length() == 0 || value == null || value.length() == 0) {
            return this;
        }
        if (value.equals(getParameters().get(key))) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new URI(protocol, username, password, host, port, path, map);
    }
    
    public URI addParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        for (Map.Entry<String, String> e : parameters.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if (value.equals(map.get(key))) {
                continue;
            }
            map.put(key, value);
        }
        return new URI(protocol, username, password, host, port, path, map);
    }

    public String getAuthority() {
        if ((username == null || username.length() == 0) && (password == null || password.length() == 0)) {
            return null;
        }
        return (username == null ? "" : username) + ":" + (password == null ? "" : password);
    }

    public int getPort(int defaultPort) {
        return port <= 0 ? defaultPort : port;
    }

    public String getAddress() {
        return (host + ":" + port);
    }
    
    private String buildString() {
        StringBuilder buf = new StringBuilder();
        if (protocol != null && protocol.length() > 0) {
            buf.append(protocol);
            buf.append("://");
        }
        if (username != null && username.length() > 0) {
            buf.append(username);
            if (password != null && password.length() > 0) {
                buf.append(":");
                buf.append(password);
            }
            buf.append("@");
        }
        String host = getHost();
        if (host != null && host.length() > 0) {
            buf.append(host);
            if (port > 0) {
                buf.append(":");
                buf.append(port);
            }
        }
        String path = getPath();
        if (path != null && path.length() > 0) {
            buf.append("/");
            buf.append(path);
        }
        buildParameters(buf, true);
        return buf.toString();
    }

    private void buildParameters(StringBuilder buf, boolean concat) {
        if (getParameters() != null && getParameters().size() > 0) {
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(getParameters()).entrySet()) {
                if (entry.getKey() != null && entry.getKey().length() > 0) {
                    if (first) {
                        if (concat) {
                            buf.append("?");
                        }
                        first = false;
                    } else {
                        buf.append("&");
                    }
                    buf.append(entry.getKey());
                    buf.append("=");
                    buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
        }
    }
    
    private String string;

    @Override
    public String toString() {
        if (this.string == null) {
            this.string = this.buildString();
        }
        return this.string;
    }

    public java.net.URL toJavaURL() {
        try {
            return new java.net.URL(toString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
