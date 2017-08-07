package io.lnk.lookup.zookeeper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.SessionExpiredException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.common.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import io.lnk.lookup.zookeeper.utils.InetSocketAddressUtils;
import io.lnk.lookup.zookeeper.utils.ZooKeeperUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2016年11月8日 下午7:11:41
 */
public class ZooKeeperClient {

    public class ZooKeeperConnectionException extends Exception {
        private static final long serialVersionUID = 5150858345927419938L;
        public ZooKeeperConnectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public interface Credentials {
        Credentials NONE = new Credentials() {
            public void authenticate(ZooKeeper zooKeeper) {}
            public String scheme() {
                return null;
            }
            public byte[] authToken() {
                return null;
            }
        };
        void authenticate(ZooKeeper zooKeeper);
        String scheme();
        byte[] authToken();
    }
    
    public static Credentials digestCredentials(String username, String password) {
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);
        return credentials("digest", (username + ":" + password).getBytes());
    }

    public static Credentials credentials(final String scheme, final byte[] authToken) {
        Preconditions.checkNotNull(scheme);
        Preconditions.checkNotNull(authToken);
        return new Credentials() {
            public void authenticate(ZooKeeper zooKeeper) {
                zooKeeper.addAuthInfo(scheme, authToken);
            }
            public String scheme() {
                return scheme;
            }
            public byte[] authToken() {
                return authToken;
            }
            public boolean equals(Object o) {
                if (!(o instanceof Credentials)) {
                    return false;
                }
                Credentials other = (Credentials) o;
                return new EqualsBuilder().append(scheme, other.scheme()).append(authToken, other.authToken()).isEquals();
            }
            public int hashCode() {
                return Objects.hashCode(scheme, authToken);
            }
        };
    }

    private final class SessionState {
        private final long sessionId;
        private final byte[] sessionPasswd;
        private SessionState(long sessionId, byte[] sessionPasswd) {
            this.sessionId = sessionId;
            this.sessionPasswd = sessionPasswd;
        }
    }
    private static final Logger log = LoggerFactory.getLogger(ZooKeeperClient.class.getName());
    private final int sessionTimeoutMillis;
    private final Credentials credentials;
    private final String zooKeeperServers;
    private volatile ZooKeeper zooKeeper;
    private SessionState sessionState;
    private Command closeEventCommand;
    
    private final Set<Watcher> watchers = new CopyOnWriteArraySet<Watcher>();
    private final BlockingQueue<WatchedEvent> eventQueue = new LinkedBlockingQueue<WatchedEvent>();
    
    private static Iterable<InetSocketAddress> combine(InetSocketAddress address, InetSocketAddress... addresses) {
        return ImmutableSet.<InetSocketAddress>builder().add(address).add(addresses).build();
    }

    public ZooKeeperClient(int sessionTimeoutMillis, InetSocketAddress zooKeeperServer, InetSocketAddress... zooKeeperServers) {
        this(sessionTimeoutMillis, combine(zooKeeperServer, zooKeeperServers));
    }

    public ZooKeeperClient(int sessionTimeoutMillis, Iterable<InetSocketAddress> zooKeeperServers) {
        this(sessionTimeoutMillis, Credentials.NONE, Optional.<String>absent(), zooKeeperServers);
    }

    public ZooKeeperClient(int sessionTimeoutMillis, Credentials credentials, InetSocketAddress zooKeeperServer, InetSocketAddress... zooKeeperServers) {
        this(sessionTimeoutMillis, credentials, Optional.<String>absent(), combine(zooKeeperServer, zooKeeperServers));
    }

    public ZooKeeperClient(int sessionTimeoutMillis, Credentials credentials, Iterable<InetSocketAddress> zooKeeperServers) {
        this(sessionTimeoutMillis, credentials, Optional.<String>absent(), zooKeeperServers);
    }

    public ZooKeeperClient(int sessionTimeoutMillis, Credentials credentials, Optional<String> chrootPath, Iterable<InetSocketAddress> zooKeeperServers) {
        this(sessionTimeoutMillis, credentials, chrootPath, Joiner.on(',').join(Iterables.transform(ImmutableSet.copyOf(zooKeeperServers), InetSocketAddressUtils.INET_TO_STR)).concat(chrootPath.or("")));
    }
    
    public ZooKeeperClient(int sessionTimeoutMillis, String zooKeeperServers) {
        this(sessionTimeoutMillis, Credentials.NONE, Optional.<String>absent(), zooKeeperServers);
    }
    
    public ZooKeeperClient(int sessionTimeoutMillis, Credentials credentials, String zooKeeperServers) {
        this(sessionTimeoutMillis, credentials, Optional.<String>absent(), zooKeeperServers);
    }
    
    public ZooKeeperClient(int sessionTimeoutMillis, Credentials credentials, Optional<String> chrootPath, String zooKeeperServers) {
        this.sessionTimeoutMillis = sessionTimeoutMillis;
        this.credentials = Preconditions.checkNotNull(credentials);
        if (chrootPath.isPresent()) {
            PathUtils.validatePath(chrootPath.get());
        }
        Preconditions.checkNotNull(zooKeeperServers);
        Preconditions.checkArgument(!StringUtils.isEmpty(zooKeeperServers), "Must present at least 1 ZK server");
        Thread watcherProcessor = new Thread("ZookeeperClient-WatcherProcessor") {
            @Override
            public void run() {
                while (true) {
                    try {
                        WatchedEvent event = eventQueue.take();
                        for (Watcher watcher : watchers) {
                            watcher.process(event);
                        }
                    } catch (InterruptedException e) {
                        /** ignore */
                    } catch (Throwable e) {
                        log.error("Watcher process Error.", e);
                    }
                }
            }
        };
        watcherProcessor.setDaemon(true);
        watcherProcessor.start();
        this.zooKeeperServers = zooKeeperServers.concat(chrootPath.or(""));
    }
    
    public boolean hasCredentials() {
        return !Strings.isNullOrEmpty(credentials.scheme()) && (credentials.authToken() != null);
    }

    public synchronized ZooKeeper get(int connectionTimeoutMillis) throws ZooKeeperConnectionException, InterruptedException, TimeoutException {
        if (zooKeeper == null) {
            final CountDownLatch connected = new CountDownLatch(1);
            Watcher watcher = new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    switch (event.getType()) {
                        case None:
                            switch (event.getState()) {
                                case Expired:
                                    log.info("Zookeeper session expired Event: {}", event);
                                    close();
                                    break;
                                case SyncConnected:
                                    connected.countDown();
                                    break;
                                default:
                                    break;
                            }
                        default:
                            break;
                    }

                    eventQueue.offer(event);
                }
            };
            try {
                zooKeeper = (sessionState != null) ? new ZooKeeper(zooKeeperServers, sessionTimeoutMillis, watcher, sessionState.sessionId, sessionState.sessionPasswd)
                        : new ZooKeeper(zooKeeperServers, sessionTimeoutMillis, watcher);
            } catch (IOException e) {
                throw new ZooKeeperConnectionException("Problem connecting to servers: " + zooKeeperServers, e);
            }

            if (connectionTimeoutMillis > 0) {
                if (!connected.await(connectionTimeoutMillis, TimeUnit.MILLISECONDS)) {
                    close();
                    throw new TimeoutException("Timed out waiting for a Zookeeper connection after " + connectionTimeoutMillis);
                }
            } else {
                try {
                    connected.await();
                } catch (InterruptedException ex) {
                    log.info("Interrupted while waiting to connect to Zookeeper");
                    close();
                    throw ex;
                }
            }
            credentials.authenticate(zooKeeper);

            sessionState = new SessionState(zooKeeper.getSessionId(), zooKeeper.getSessionPasswd());
        }
        return zooKeeper;
    }

    public Watcher registerExpirationHandler(final Command onExpired) {
        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getType() == EventType.None && event.getState() == KeeperState.Expired) {
                    onExpired.execute();
                }
            }
        };
        register(watcher);
        return watcher;
    }
    
    public void registerCloseEventExpirationHandler(Command closeEventCommand) {
        this.closeEventCommand = closeEventCommand;
    }

    public void register(Watcher watcher) {
        watchers.add(watcher);
    }

    public boolean unregister(Watcher watcher) {
        return watchers.remove(watcher);
    }

    public boolean shouldRetry(KeeperException e) {
        if (e instanceof SessionExpiredException) {
            close();
        }
        return ZooKeeperUtils.isRetryable(e);
    }

    public synchronized void close() {
        if (zooKeeper != null) {
            try {
                zooKeeper.close();
            } catch (Throwable e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted trying to close Zookeeper", e);
            } finally {
                zooKeeper = null;
                sessionState = null;
            }
            try {
                closeEventCommand.execute();
            } catch (Throwable e) {
                log.warn("Invoking Zookeeper close Event Command Error.", e);
            }
        }
    }

    public synchronized boolean isClosed() {
        return zooKeeper == null;
    }

    public String getZooKeeperServers() {
        return zooKeeperServers;
    }
}
