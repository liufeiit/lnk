package io.lnk.remoting.mina;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.remoting.ClientConfiguration;
import io.lnk.remoting.CommandProcessor;
import io.lnk.remoting.Pair;
import io.lnk.remoting.RemotingCallback;
import io.lnk.remoting.RemotingClient;
import io.lnk.remoting.exception.RemotingConnectException;
import io.lnk.remoting.exception.RemotingSendRequestException;
import io.lnk.remoting.exception.RemotingTimeoutException;
import io.lnk.remoting.mina.codec.CommandProtocolCodecFilter;
import io.lnk.remoting.protocol.RemotingCommand;
import io.lnk.remoting.utils.RemotingThreadFactory;
import io.lnk.remoting.utils.RemotingUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月19日 下午6:54:59
 */
public class MinaRemotingClient extends MinaAbstractRemotingService implements RemotingClient {
    private static final long LOCK_TIMEOUT_MILLIS = 3000;
    private final ClientConfiguration configuration;
    private final NioSocketConnector connector;
    private final Lock lock = new ReentrantLock();
    private final ConcurrentHashMap<String, RemotingSessionFuture> sessions = new ConcurrentHashMap<String, RemotingSessionFuture>();
    private final ExecutorService defaultThreadPoolExecutor;

    public MinaRemotingClient(final ProtocolFactorySelector protocolFactorySelector, final ClientConfiguration configuration) {
        super(protocolFactorySelector);
        this.configuration = configuration;
        this.connector = new NioSocketConnector(configuration.getWorkerThreads());
        this.defaultThreadPoolExecutor =
                Executors.newFixedThreadPool(configuration.getDefaultExecutorThreads(), RemotingThreadFactory.newThreadFactory("MinaRemotingClientDefaultThreadPoolExecutor-%d", false));
    }

    @Override
    public void start() {
        this.connector.getFilterChain().addLast("exceutor", new ExecutorFilter(
                Executors.newFixedThreadPool(configuration.getDefaultExecutorThreads(), RemotingThreadFactory.newThreadFactory("MinaRemotingClientDefaultThreadPoolExecutor-%d", false))));
        this.connector.getFilterChain().addLast("mdc", new MdcInjectionFilter());
        this.connector.getFilterChain().addLast("logger", new LoggingFilter());
        this.connector.getFilterChain().addLast("codec", new CommandProtocolCodecFilter());
        this.connector.setConnectTimeoutMillis(configuration.getConnectTimeoutMillis());
        this.connector.getSessionConfig().setReuseAddress(true);
        this.connector.getSessionConfig().setReadBufferSize(configuration.getSocketRcvBufSize());
        this.connector.getSessionConfig().setReceiveBufferSize(configuration.getSocketRcvBufSize());
        this.connector.getSessionConfig().setSendBufferSize(configuration.getSocketSndBufSize());
        this.connector.getSessionConfig().setTcpNoDelay(true);
        this.connector.getSessionConfig().setSoLinger(-1);
        this.connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, configuration.getChannelMaxIdleTimeSeconds());
        this.connector.setHandler(new ClientIoHandler());
    }

    @Override
    public RemotingCommand invokeSync(String addr, RemotingCommand request, long timeoutMillis)
            throws InterruptedException, RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException {
        final IoSession session = this.getAndCreateSession(addr);
        if (session != null && session.isConnected()) {
            try {
                return this.__invokeSync(session, request, timeoutMillis);
            } catch (RemotingSendRequestException e) {
                log.warn("send command Error, so close the channel[{}]", addr);
                this.close(addr, session);
                throw e;
            } catch (RemotingTimeoutException e) {
                this.close(addr, session);
                log.warn("close channel because of timeout, used {} ms, {}", timeoutMillis, addr);
                log.warn("wait command timeout Error, the channel[{}]", addr);
                throw e;
            }
        } else {
            this.close(addr, session);
            throw new RemotingConnectException(addr);
        }
    }

    @Override
    public void invokeAsync(String addr, RemotingCommand request, long timeoutMillis, RemotingCallback callback)
            throws InterruptedException, RemotingConnectException, RemotingTimeoutException, RemotingSendRequestException {
        final IoSession session = this.getAndCreateSession(addr);
        if (session != null && session.isConnected()) {
            try {
                this.__invokeAsync(session, request, timeoutMillis, callback);
            } catch (RemotingSendRequestException e) {
                log.warn("send command Error, so close the channel[{}]", addr);
                this.close(addr, session);
                throw e;
            }
        } else {
            this.close(addr, session);
            throw new RemotingConnectException(addr);
        }
    }

    @Override
    public void invokeOneway(String addr, RemotingCommand request) throws InterruptedException, RemotingConnectException, RemotingSendRequestException {
        final IoSession session = this.getAndCreateSession(addr);
        if (session != null && session.isConnected()) {
            try {
                this.__invokeOneway(session, request);
            } catch (RemotingSendRequestException e) {
                log.warn("send command Error, so close the channel[{}]", addr);
                this.close(addr, session);
                throw e;
            }
        } else {
            this.close(addr, session);
            throw new RemotingConnectException(addr);
        }
    }

    @Override
    public void registerProcessor(int commandCode, CommandProcessor processor, ExecutorService executor) {
        ExecutorService executorPair = (executor == null) ? defaultThreadPoolExecutor : executor;
        Pair<CommandProcessor, ExecutorService> pair = new Pair<CommandProcessor, ExecutorService>(processor, executorPair);
        this.processors.put(commandCode, pair);
    }

    @Override
    protected ExecutorService getCallbackExecutor() {
        return this.defaultThreadPoolExecutor;
    }

    @Override
    public void shutdown() {
        try {
            for (RemotingSessionFuture remotingSessionFuture : this.sessions.values()) {
                this.close(null, remotingSessionFuture.getSession());
            }
            this.sessions.clear();
            this.connector.dispose();
        } catch (Throwable e) {
            log.error("MinaRemotingClient shutdown Error.", e);
        }
        if (this.defaultThreadPoolExecutor != null) {
            try {
                this.defaultThreadPoolExecutor.shutdown();
            } catch (Throwable e) {
                log.error("MinaRemotingClient shutdown Error.", e);
            }
        }
    }

    private void close(final String addr, final IoSession session) {
        if (session == null) {
            this.sessions.remove(addr);
            return;
        }
        final String addrRemote = (addr == null) ? RemotingUtils.parseSessionRemoteAddr(session) : addr;
        try {
            if (this.lock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean close = true;
                    final RemotingSessionFuture remotingChannelFuture = this.sessions.get(addrRemote);
                    if (null == remotingChannelFuture) {
                        close = false;
                    } else if (remotingChannelFuture.getSession() != session) {
                        close = false;
                    }
                    if (close) {
                        this.sessions.remove(addrRemote);
                    }
                    session.closeOnFlush();
                } catch (Throwable e) {
                    log.error("close the channel[" + addrRemote + "] Error.", e);
                } finally {
                    this.lock.unlock();
                }
            } else {
                log.warn("try to lock channels, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
            }
        } catch (InterruptedException e) {
            log.error("close channel Error.", e);
        }
    }

    public void close(final IoSession session) {
        if (session == null) {
            return;
        }
        try {
            if (this.lock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean close = true;
                    RemotingSessionFuture remotingChannelFuture = null;
                    String addrRemote = null;
                    for (Map.Entry<String, RemotingSessionFuture> entry : sessions.entrySet()) {
                        String key = entry.getKey();
                        RemotingSessionFuture prev = entry.getValue();
                        if (prev.getSession() != null) {
                            if (prev.getSession() == session) {
                                remotingChannelFuture = prev;
                                addrRemote = key;
                                break;
                            }
                        }
                    }
                    if (null == remotingChannelFuture) {
                        close = false;
                    }
                    if (close) {
                        this.sessions.remove(addrRemote);
                        session.closeOnFlush();
                    }
                } catch (Throwable e) {
                    log.error("close the channel Error.", e);
                } finally {
                    this.lock.unlock();
                }
            } else {
                log.warn("try to lock channels, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
            }
        } catch (InterruptedException e) {
            log.error("close channel Error.", e);
        }
    }

    private IoSession getAndCreateSession(final String addr) throws InterruptedException {
        RemotingSessionFuture remotingChannelFuture = this.sessions.get(addr);
        if (remotingChannelFuture != null && remotingChannelFuture.isActive()) {
            return remotingChannelFuture.getSession();
        }
        return this.createChannel(addr);
    }

    private IoSession createChannel(final String addr) throws InterruptedException {
        RemotingSessionFuture remotingSessionFuture = this.sessions.get(addr);
        if (remotingSessionFuture != null && remotingSessionFuture.isActive()) {
            return remotingSessionFuture.getSession();
        }
        if (this.lock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection = false;
                remotingSessionFuture = this.sessions.get(addr);
                if (remotingSessionFuture != null) {
                    if (remotingSessionFuture.isActive()) {
                        return remotingSessionFuture.getSession();
                    } else if (!remotingSessionFuture.getConnectFuture().isDone()) {
                        createNewConnection = false;
                    } else {
                        this.sessions.remove(addr);
                        createNewConnection = true;
                        remotingSessionFuture = this.sessions.remove(addr);
                        if (remotingSessionFuture != null) {
                            try {
                                IoSession session = remotingSessionFuture.getSession();
                                session.closeOnFlush();
                                session = null;
                                remotingSessionFuture = null;
                            } catch (Throwable ignore) {
                            }
                        }
                    }
                } else {
                    createNewConnection = true;
                }
                if (createNewConnection) {
                    ConnectFuture connectFuture = this.connector.connect(RemotingUtils.string2SocketAddress(addr));
                    log.info("begin to connect remote host[{}] asynchronously", addr);
                    remotingSessionFuture = new RemotingSessionFuture(connectFuture);
                    this.sessions.put(addr, remotingSessionFuture);
                }
            } catch (Throwable e) {
                log.error("create channel Error.", e);
            } finally {
                this.lock.unlock();
            }
        } else {
            log.warn("try to lock channels, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
        }
        if (remotingSessionFuture != null) {
            ConnectFuture connectFuture = remotingSessionFuture.getConnectFuture();
            if (connectFuture.awaitUninterruptibly(this.configuration.getConnectTimeoutMillis())) {
                if (remotingSessionFuture.isActive()) {
                    log.info("connect remote host[{}] success, {}", addr, connectFuture.toString());
                    return remotingSessionFuture.getSession();
                } else {
                    log.warn("connect remote host[" + addr + "] Error, " + connectFuture.toString(), connectFuture.getException());
                }
            } else {
                log.warn("connect remote host[{}] timeout {}ms, {}", addr, this.configuration.getConnectTimeoutMillis(), connectFuture.toString());
            }
        }
        return null;
    }

    private static class RemotingSessionFuture {
        private final ConnectFuture connectFuture;

        public RemotingSessionFuture(ConnectFuture connectFuture) {
            this.connectFuture = connectFuture;
        }

        public boolean isActive() {
            return this.connectFuture.isConnected();
        }

        private IoSession getSession() {
            return this.connectFuture.getSession();
        }

        public ConnectFuture getConnectFuture() {
            return connectFuture;
        }
    }

    class ClientIoHandler extends IoHandlerAdapter {
        public void messageReceived(IoSession session, Object message) throws Exception {
            commandProcessor(session, (RemotingCommand) message);
        }

        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            final String remoteAddress = RemotingUtils.parseSessionRemoteAddr(session);
            log.warn("MinaRemotingClient pipeline: exceptionCaught {}", remoteAddress);
            log.warn("MinaRemotingClient pipeline: exceptionCaught Error.", cause);
            MinaRemotingClient.this.close(session);
        }

        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
            if (status == IdleStatus.BOTH_IDLE) {
                MinaRemotingClient.this.close(session);
            }
        }

        public void sessionClosed(IoSession session) throws Exception {
            final String remoteAddress = RemotingUtils.parseSessionRemoteAddr(session);
            log.info("MinaRemotingClient pipeline: close {}", remoteAddress);
            MinaRemotingClient.this.close(session);
            super.sessionClosed(session);
        }
    }
}
