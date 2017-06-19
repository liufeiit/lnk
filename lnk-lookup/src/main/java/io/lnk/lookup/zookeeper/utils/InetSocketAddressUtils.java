package io.lnk.lookup.zookeeper.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public final class InetSocketAddressUtils {

    public static final Function<String, InetSocketAddress> STR_TO_INET = new Function<String, InetSocketAddress>() {
        public InetSocketAddress apply(String value) {
            return parse(value);
        }
    };

    public static final Function<Integer, InetSocketAddress> INT_TO_INET = new Function<Integer, InetSocketAddress>() {
        public InetSocketAddress apply(Integer port) {
            try {
                return getLocalAddress(port);
            } catch (UnknownHostException e) {
                throw Throwables.propagate(e);
            }
        }
    };

    public static final Function<InetSocketAddress, String> INET_TO_STR = new Function<InetSocketAddress, String>() {
        public String apply(InetSocketAddress addr) {
            return InetSocketAddressUtils.toString(addr);
        }
    };

    public static InetSocketAddress parse(String value) {
        Preconditions.checkNotNull(value);
        String[] spec = value.split(":", 2);
        if (spec.length != 2) {
            throw new IllegalArgumentException("Invalid socket address spec: " + value);
        }
        String host = spec[0];
        int port = asPort(spec[1]);
        return StringUtils.isEmpty(host) ? new InetSocketAddress(port) : InetSocketAddress.createUnresolved(host, port);
    }

    public static String toString(InetSocketAddress value) {
        Preconditions.checkNotNull(value);
        return value.getHostName() + ":" + value.getPort();
    }

    private static int asPort(String port) {
        if ("*".equals(port)) {
            return 0;
        }
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port: " + port, e);
        }
    }

    public static InetSocketAddress getLocalAddress(int port) throws UnknownHostException {
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        return new InetSocketAddress(ipAddress, port);
    }

    public static Set<InetSocketAddress> convertToSockets(Iterable<String> backends) {
        return Sets.newHashSet(Iterables.transform(backends, STR_TO_INET));
    }

    private InetSocketAddressUtils() {}
}
