//package io.lnk.remoting.mina;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//import org.apache.mina.core.future.ConnectFuture;
//import org.apache.mina.core.service.IoHandlerAdapter;
//import org.apache.mina.core.session.IdleStatus;
//import org.apache.mina.core.session.IoSession;
//import org.apache.mina.transport.socket.nio.NioSocketConnector;
//
//import io.lnk.api.protocol.ProtocolFactorySelector;
//import io.lnk.remoting.CommandProcessor;
//import io.lnk.remoting.Pair;
//import io.lnk.remoting.RemotingCallback;
//import io.lnk.remoting.RemotingClient;
//import io.lnk.remoting.exception.RemotingConnectException;
//import io.lnk.remoting.exception.RemotingSendRequestException;
//import io.lnk.remoting.exception.RemotingTimeoutException;
//import io.lnk.remoting.protocol.RemotingCommand;
//import io.lnk.remoting.utils.RemotingThreadFactory;
//import io.lnk.remoting.utils.RemotingUtils;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelFuture;
//import io.netty.util.concurrent.DefaultEventExecutorGroup;
//
///**
// * @author 刘飞 E-mail:liufei_it@126.com
// *
// * @version 1.0.0
// * @since 2017年5月19日 下午6:54:59
// */
//public class MinaRemotingClient extends MinaAbstractRemotingService implements RemotingClient {
//    private static final long LOCK_TIMEOUT_MILLIS = 3000;
//    private final MinaClientConfiguration configuration;
//    private final NioSocketConnector connector = new NioSocketConnector();
//    private final Lock lock = new ReentrantLock();
//    private final ConcurrentHashMap<String, RemotingChannelFuture> channels = new ConcurrentHashMap<String, RemotingChannelFuture>();
//    private final ExecutorService defaultThreadPoolExecutor;
//    private DefaultEventExecutorGroup defaultEventExecutorGroup;
//
//    public MinaRemotingClient(final ProtocolFactorySelector protocolFactorySelector, final MinaClientConfiguration configuration) {
//        super(protocolFactorySelector);
//        this.configuration = configuration;
//        this.defaultThreadPoolExecutor = Executors.newFixedThreadPool(configuration.getDefaultExecutorThreads(), RemotingThreadFactory.newThreadFactory("MinaRemotingClientDefaultThreadPoolExecutor-%d", false));
//    }
//
//    @Override
//    public void start() {
//        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(configuration.getWorkerThreads(), RemotingThreadFactory.newThreadFactory("MinaRemotingWorker-%d", false));
//        this.connector.setConnectTimeoutMillis(configuration.getConnectTimeoutMillis());
//        this.connector.setHandler(new ClientIoHandler());
//        
//        this.connector.connect(remoteAddress);
//    }
//
//    @Override
//    public RemotingCommand invokeSync(String addr, RemotingCommand request, long timeoutMillis) throws InterruptedException, RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException {
//        final Channel channel = this.getAndCreateChannel(addr);
//        if (channel != null && channel.isActive()) {
//            try {
//                return this.__invokeSync(channel, request, timeoutMillis);
//            } catch (RemotingSendRequestException e) {
//                log.warn("send command Error, so close the channel[{}]", addr);
//                this.close(addr, channel);
//                throw e;
//            } catch (RemotingTimeoutException e) {
//                this.close(addr, channel);
//                log.warn("close channel because of timeout, used {} ms, {}", timeoutMillis, addr);
//                log.warn("wait command timeout Error, the channel[{}]", addr);
//                throw e;
//            }
//        } else {
//            this.close(addr, channel);
//            throw new RemotingConnectException(addr);
//        }
//    }
//
//    @Override
//    public void invokeAsync(String addr, RemotingCommand request, long timeoutMillis, RemotingCallback callback) throws InterruptedException, RemotingConnectException, RemotingTimeoutException, RemotingSendRequestException {
//        final Channel channel = this.getAndCreateChannel(addr);
//        if (channel != null && channel.isActive()) {
//            try {
//                this.__invokeAsync(channel, request, timeoutMillis, callback);
//            } catch (RemotingSendRequestException e) {
//                log.warn("send command Error, so close the channel[{}]", addr);
//                this.close(addr, channel);
//                throw e;
//            }
//        } else {
//            this.close(addr, channel);
//            throw new RemotingConnectException(addr);
//        }
//    }
//
//    @Override
//    public void invokeOneway(String addr, RemotingCommand request) throws InterruptedException, RemotingConnectException, RemotingSendRequestException {
//        final Channel channel = this.getAndCreateChannel(addr);
//        if (channel != null && channel.isActive()) {
//            try {
//                this.__invokeOneway(channel, request);
//            } catch (RemotingSendRequestException e) {
//                log.warn("send command Error, so close the channel[{}]", addr);
//                this.close(addr, channel);
//                throw e;
//            }
//        } else {
//            this.close(addr, channel);
//            throw new RemotingConnectException(addr);
//        }
//    }
//
//    @Override
//    public void registerProcessor(int commandCode, CommandProcessor processor, ExecutorService executor) {
//        ExecutorService executorPair = (executor == null) ? defaultThreadPoolExecutor : executor;
//        Pair<CommandProcessor, ExecutorService> pair = new Pair<CommandProcessor, ExecutorService>(processor, executorPair);
//        this.processors.put(commandCode, pair);
//    }
//
//    @Override
//    protected ExecutorService getCallbackExecutor() {
//        return this.defaultThreadPoolExecutor;
//    }
//
//    @Override
//    public void shutdown() {
//        try {
//            for (RemotingChannelFuture remotingChannelFuture : this.channels.values()) {
//                this.close(null, remotingChannelFuture.getChannel());
//            }
//            this.channels.clear();
//            this.eventLoopGroupWorker.shutdownGracefully();
//            if (this.defaultEventExecutorGroup != null) {
//                this.defaultEventExecutorGroup.shutdownGracefully();
//            }
//        } catch (Throwable e) {
//            log.error("NettyRemotingClient shutdown Error.", e);
//        }
//        if (this.defaultThreadPoolExecutor != null) {
//            try {
//                this.defaultThreadPoolExecutor.shutdown();
//            } catch (Throwable e) {
//                log.error("NettyRemotingServer shutdown Error.", e);
//            }
//        }
//    }
//
//    private void close(final String addr, final Channel channel) {
//        if (channel == null) {
//            this.channels.remove(addr);
//            return;
//        }
//        final String addrRemote = (addr == null) ? RemotingUtils.parseChannelRemoteAddr(channel) : addr;
//        try {
//            if (this.lock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
//                try {
//                    boolean close = true;
//                    final RemotingChannelFuture remotingChannelFuture = this.channels.get(addrRemote);
//                    if (null == remotingChannelFuture) {
//                        close = false;
//                    } else if (remotingChannelFuture.getChannel() != channel) {
//                        close = false;
//                    }
//                    if (close) {
//                        this.channels.remove(addrRemote);
//                    }
//                    RemotingUtils.closeChannel(channel);
//                } catch (Throwable e) {
//                    log.error("close the channel[" + addrRemote +  "] Error.", e);
//                } finally {
//                    this.lock.unlock();
//                }
//            } else {
//                log.warn("try to lock channels, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
//            }
//        } catch (InterruptedException e) {
//            log.error("close channel Error.", e);
//        }
//    }
//
//    public void close(final Channel channel) {
//        if (channel == null) {
//            return;
//        }
//        try {
//            if (this.lock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
//                try {
//                    boolean close = true;
//                    RemotingChannelFuture remotingChannelFuture = null;
//                    String addrRemote = null;
//                    for (Map.Entry<String, RemotingChannelFuture> entry : channels.entrySet()) {
//                        String key = entry.getKey();
//                        RemotingChannelFuture prev = entry.getValue();
//                        if (prev.getChannel() != null) {
//                            if (prev.getChannel() == channel) {
//                                remotingChannelFuture = prev;
//                                addrRemote = key;
//                                break;
//                            }
//                        }
//                    }
//                    if (null == remotingChannelFuture) {
//                        close = false;
//                    }
//                    if (close) {
//                        this.channels.remove(addrRemote);
//                        RemotingUtils.closeChannel(channel);
//                    }
//                } catch (Throwable e) {
//                    log.error("close the channel Error.", e);
//                } finally {
//                    this.lock.unlock();
//                }
//            } else {
//                log.warn("try to lock channels, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
//            }
//        } catch (InterruptedException e) {
//            log.error("close channel Error.", e);
//        }
//    }
//
//    private Channel getAndCreateChannel(final String addr) throws InterruptedException {
//        RemotingChannelFuture remotingChannelFuture = this.channels.get(addr);
//        if (remotingChannelFuture != null && remotingChannelFuture.isActive()) {
//            return remotingChannelFuture.getChannel();
//        }
//        return this.createChannel(addr);
//    }
//
//    private Channel createChannel(final String addr) throws InterruptedException {
//        RemotingChannelFuture remotingChannelFuture = this.channels.get(addr);
//        if (remotingChannelFuture != null && remotingChannelFuture.isActive()) {
//            return remotingChannelFuture.getChannel();
//        }
//        if (this.lock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
//            try {
//                boolean createNewConnection = false;
//                remotingChannelFuture = this.channels.get(addr);
//                if (remotingChannelFuture != null) {
//                    if (remotingChannelFuture.isActive()) {
//                        return remotingChannelFuture.getChannel();
//                    } else if (!remotingChannelFuture.getChannelFuture().isDone()) {
//                        createNewConnection = false;
//                    } else {
//                        this.channels.remove(addr);
//                        createNewConnection = true;
//                        remotingChannelFuture = this.channels.remove(addr);
//                        if (remotingChannelFuture != null) {
//                            try {
//                                Channel channel = remotingChannelFuture.getChannel();
//                                channel.close();
//                                channel = null;
//                                remotingChannelFuture = null;
//                            } catch (Throwable ignore) {}
//                        }
//                    }
//                } else {
//                    createNewConnection = true;
//                }
//                if (createNewConnection) {
//                    ChannelFuture channelFuture = this.connector.connect(RemotingUtils.string2SocketAddress(addr));
//                    log.info("begin to connect remote host[{}] asynchronously", addr);
//                    remotingChannelFuture = new RemotingChannelFuture(channelFuture);
//                    this.channels.put(addr, remotingChannelFuture);
//                }
//            } catch (Throwable e) {
//                log.error("create channel Error.", e);
//            } finally {
//                this.lock.unlock();
//            }
//        } else {
//            log.warn("try to lock channels, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
//        }
//        if (remotingChannelFuture != null) {
//            ChannelFuture channelFuture = remotingChannelFuture.getChannelFuture();
//            if (channelFuture.awaitUninterruptibly(this.configuration.getConnectTimeoutMillis())) {
//                if (remotingChannelFuture.isActive()) {
//                    log.info("connect remote host[{}] success, {}", addr, channelFuture.toString());
//                    return remotingChannelFuture.getChannel();
//                } else {
//                    log.warn("connect remote host[" + addr + "] Error, " + channelFuture.toString(), channelFuture.cause());
//                }
//            } else {
//                log.warn("connect remote host[{}] timeout {}ms, {}", addr, this.configuration.getConnectTimeoutMillis(), channelFuture.toString());
//            }
//        }
//        return null;
//    }
//
//    private static class RemotingChannelFuture {
//        private final ConnectFuture connectFuture;
//        public RemotingChannelFuture(ConnectFuture connectFuture, long timeout) {
//            this.connectFuture = connectFuture;
//            this.connectFuture.awaitUninterruptibly(timeout, TimeUnit.MILLISECONDS);
//        }
//
//        public boolean isActive() {
//            return this.connectFuture.isConnected();
//        }
//
//        private IoSession getChannel() {
//            return this.connectFuture.getSession();
//        }
//
//        public ConnectFuture getConnectFuture() {
//            return connectFuture;
//        }
//    }
//
//    class ClientIoHandler extends IoHandlerAdapter {
//        public void messageReceived(IoSession session, Object message) throws Exception {
//            commandProcessor(session, (RemotingCommand) message);
//        }
//        
//        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
//            final String remoteAddress = RemotingUtils.parseChannelRemoteAddr(session);
//            log.warn("MinaRemotingClient pipeline: exceptionCaught {}", remoteAddress);
//            log.warn("MinaRemotingClient pipeline: exceptionCaught Error.", cause);
//            MinaRemotingClient.this.close(session);
//        }
//
//        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
//            if (status == IdleStatus.BOTH_IDLE) {
//                MinaRemotingClient.this.close(session);
//            }
//        }
//        
//        public void sessionClosed(IoSession session) throws Exception {
//            final String remoteAddress = RemotingUtils.parseChannelRemoteAddr(session);
//            log.info("MinaRemotingClient pipeline: close {}", remoteAddress);
//            MinaRemotingClient.this.close(session);
//            super.close(session);
//        }
//    }
//}
