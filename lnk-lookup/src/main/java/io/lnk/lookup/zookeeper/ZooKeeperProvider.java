package io.lnk.lookup.zookeeper;

import java.nio.charset.Charset;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import io.lnk.api.URI;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午6:06:52
 */
public class ZooKeeperProvider {
    protected final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());
    private static final String MAX_RETRIES = "maxRetries";
    private static final String SLEEP_TIME_MILLIS = "sleepTimeMillis";
    private static final String CHARSET = "charset";
    private static final String SESSION_TIMEOUT_MILLIS = "sessionTimeoutMillis";
    private static final String CONNECT_TIMEOUT_MILLIS = "connectTimeoutMillis";
    protected final URI uri;
    private final CuratorFramework client;
    private final int connectTimeoutMillis;
    private final int sessionTimeoutMillis;
    private final String connectString;
    private final String namespace;
    private final Charset charset;
    
    public ZooKeeperProvider(URI uri, String namespace) {
        this(uri, uri.getInt(CONNECT_TIMEOUT_MILLIS, 30000), uri.getInt(SESSION_TIMEOUT_MILLIS, 30000), uri.getAddress(), namespace);
    }
    
    public ZooKeeperProvider(URI uri, int connectionTimeoutMillis, int sessionTimeoutMillis, String connectString, String namespace) {
        this(uri, connectionTimeoutMillis, sessionTimeoutMillis, connectString, namespace, uri.getCharset(CHARSET, Charsets.UTF_8));
    }
    
    public ZooKeeperProvider(URI uri, int connectTimeoutMillis, int sessionTimeoutMillis, String connectString, String namespace, Charset charset) {
        super();
        this.uri = uri;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.sessionTimeoutMillis = sessionTimeoutMillis;
        this.connectString = connectString;
        this.namespace = namespace;
        this.charset = charset;
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(this.uri.getInt(SLEEP_TIME_MILLIS, 1000), this.uri.getInt(MAX_RETRIES, 29));
        this.client = this.createWithOptions(this.connectString, retryPolicy, this.connectTimeoutMillis, this.sessionTimeoutMillis, this.namespace);
        this.client.start();
    }

    private final CuratorFramework createWithOptions(String connectString, RetryPolicy retryPolicy, int connectTimeoutMillis, int sessionTimeoutMillis, String namespace) {
        return CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .namespace(namespace)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectTimeoutMillis)
                .sessionTimeoutMs(sessionTimeoutMillis)
                .build();
    }
}
