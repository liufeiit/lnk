package io.lnk.api;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午2:29:08
 */
public class Address implements Comparable<Address>, Serializable {
    private static final long serialVersionUID = -3826228304734021497L;
    private String host;
    private int port;

    public Address() {
        super();
    }
    
    public Address(String addr) {
        String[] addrs = StringUtils.split(addr, ":");
        this.setHost(addrs[0]);
        this.setPort(Integer.parseInt(addrs[1]));
    }

    public Address(String host, int port) {
        super();
        this.host = host;
        this.port = port;
    }

    @Override
    public int compareTo(Address o) {
        return this.toString().compareTo(o.toString());
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Address other = (Address) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (port != other.port)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
