package io.lnk.remoting.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.remoting.CommandProcessor;
import io.lnk.remoting.Pair;
import io.lnk.remoting.RemotingCallback;
import io.lnk.remoting.RemotingClient;
import io.lnk.remoting.exception.RemotingConnectException;
import io.lnk.remoting.exception.RemotingSendRequestException;
import io.lnk.remoting.exception.RemotingTimeoutException;
import io.lnk.remoting.netty.codec.ProtocolDecoder;
import io.lnk.remoting.netty.codec.ProtocolEncoder;
import io.lnk.remoting.protocol.RemotingCommand;
import io.lnk.remoting.utils.RemotingThreadFactory;
import io.lnk.remoting.utils.RemotingUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月19日 下午6:54:59
 */
public class NettyRemotingClient extends NettyAbstractRemotingService implements RemotingClient {
    private static final long LOCK_TIMEOUT_MILLIS = 3000;
    private final NettyClientConfiguration configuration;
    private final Bootstrap bootstrap = new Bootstrap();
    private final EventLoopGroup eventLoopGroupWorker;
    private final Lock lock = new ReentrantLock();
    private final ConcurrentHashMap<String, RemotingChannelFuture> channels = new ConcurrentHashMap<String, RemotingChannelFuture>();
    private final ExecutorService defaultThreadPoolExecutor;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    public NettyRemotingClient(final ProtocolFactorySelector protocolFactorySelector, final NettyClientConfiguration configuration) {
        super(protocolFactorySelector);
        this.configuration = configuration;
        this.defaultThreadPoolExecutor = Executors.newFixedThreadPool(configuration.getDefaultExecutorThreads(), RemotingThreadFactory.newThreadFactory("NettyRemotingClientDefaultThreadPoolExecutor-%d", false));
        this.eventLoopGroupWorker = new NioEventLoopGroup(1, RemotingThreadFactory.newThreadFactory("NettyRemotingClientSelector-%d", false));
    }

    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(configuration.getWorkerThreads(), RemotingThreadFactory.newThreadFactory("NettyRemotingWorker-%d", false));
        this.bootstrap.group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, configuration.getConnectTimeoutMillis())
                .option(ChannelOption.SO_SNDBUF, configuration.getSocketSndBufSize())
                .option(ChannelOption.SO_RCVBUF, configuration.getSocketRcvBufSize())
                .handler(new ChannelInitializer<SocketChannel>() {
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(defaultEventExecutorGroup, new ProtocolEncoder(), new ProtocolDecoder(),
                                new IdleStateHandler(0, 0, configuration.getChannelMaxIdleTimeSeconds()), 
                                new NettyConnectManageHandler(), new NettyClientHandler());
                    }
                });
    }

    @Override
    public RemotingCommand invokeSync(String addr, RemotingCommand request, long timeoutMillis) throws InterruptedException, RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException {
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            try {
                return this.__invokeSync(channel, request, timeoutMillis);
            } catch (RemotingSendRequestException e) {
                log.warn("send command Error, so close the channel[{}]", addr);
                this.close(addr, channel);
                throw e;
            } catch (RemotingTimeoutException e) {
                this.close(addr, channel);
                log.warn("close channel because of timeout, used {} ms, {}", timeoutMillis, addr);
                log.warn("wait command timeout Error, the channel[{}]", addr);
                throw e;
            }
        } else {
            this.close(addr, channel);
            throw new RemotingConnectException(addr);
        }
    }

    @Override
    public void invokeAsync(String addr, RemotingCommand request, long timeoutMillis, RemotingCallback callback) throws InterruptedException, RemotingConnectException, RemotingTimeoutException, RemotingSendRequestException {
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            try {
                this.__invokeAsync(channel, request, timeoutMillis, callback);
            } catch (RemotingSendRequestException e) {
                log.warn("send command Error, so close the channel[{}]", addr);
                this.close(addr, channel);
                throw e;
            }
        } else {
            this.close(addr, channel);
            throw new RemotingConnectException(addr);
        }
    }

    @Override
    public void invokeOneway(String addr, RemotingCommand request) throws InterruptedException, RemotingConnectException, RemotingSendRequestException {
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            try {
                this.__invokeOneway(channel, request);
            } catch (RemotingSendRequestException e) {
                log.warn("send command Error, so close the channel[{}]", addr);
                this.close(addr, channel);
                throw e;
            }
        } else {
            this.close(addr, channel);
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
            for (RemotingChannelFuture remotingChannelFuture : this.channels.values()) {
                this.close(null, remotingChannelFuture.getChannel());
            }
            this.channels.clear();
            this.eventLoopGroupWorker.shutdownGracefully();
            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Throwable e) {
            log.error("NettyRemotingClient shutdown Error.", e);
        }
        if (this.defaultThreadPoolExecutor != null) {
            try {
                this.defaultThreadPoolExecutor.shutdown();
            } catch (Throwable e) {
                log.error("NettyRemotingClient shutdown Error.", e);
            }
        }
    }

    private void close(final String addr, final Channel channel) {
        if (channel == null) {
            this.channels.remove(addr);
            return;
        }
        final String addrRemote = (addr == null) ? RemotingUtils.parseChannelRemoteAddr(channel) : addr;
        try {
            if (this.lock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean close = true;
                    final RemotingChannelFuture remotingChannelFuture = this.channels.get(addrRemote);
                    if (null == remotingChannelFuture) {
                        close = false;
                    } else if (remotingChannelFuture.getChannel() != channel) {
                        close = false;
                    }
                    if (close) {
                        this.channels.remove(addrRemote);
                    }
                    RemotingUtils.closeChannel(channel);
                } catch (Throwable e) {
                    log.error("close the channel[" + addrRemote +  "] Error.", e);
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

    public void close(final Channel channel) {
        if (channel == null) {
            return;
        }
        try {
            if (this.lock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean close = true;
                    RemotingChannelFuture remotingChannelFuture = null;
                    String addrRemote = null;
                    for (Map.Entry<String, RemotingChannelFuture> entry : channels.entrySet()) {
                        String key = entry.getKey();
                        RemotingChannelFuture prev = entry.getValue();
                        if (prev.getChannel() != null) {
                            if (prev.getChannel() == channel) {
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
                        this.channels.remove(addrRemote);
                        RemotingUtils.closeChannel(channel);
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

    private Channel getAndCreateChannel(final String addr) throws InterruptedException {
        RemotingChannelFuture remotingChannelFuture = this.channels.get(addr);
        if (remotingChannelFuture != null && remotingChannelFuture.isActive()) {
            return remotingChannelFuture.getChannel();
        }
        return this.createChannel(addr);
    }

    private Channel createChannel(final String addr) throws InterruptedException {
        RemotingChannelFuture remotingChannelFuture = this.channels.get(addr);
        if (remotingChannelFuture != null && remotingChannelFuture.isActive()) {
            return remotingChannelFuture.getChannel();
        }
        if (this.lock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection = false;
                remotingChannelFuture = this.channels.get(addr);
                if (remotingChannelFuture != null) {
                    if (remotingChannelFuture.isActive()) {
                        return remotingChannelFuture.getChannel();
                    } else if (!remotingChannelFuture.getChannelFuture().isDone()) {
                        createNewConnection = false;
                    } else {
                        this.channels.remove(addr);
                        createNewConnection = true;
                        remotingChannelFuture = this.channels.remove(addr);
                        if (remotingChannelFuture != null) {
                            try {
                                Channel channel = remotingChannelFuture.getChannel();
                                channel.close();
                                channel = null;
                                remotingChannelFuture = null;
                            } catch (Throwable ignore) {}
                        }
                    }
                } else {
                    createNewConnection = true;
                }
                if (createNewConnection) {
                    ChannelFuture channelFuture = this.bootstrap.connect(RemotingUtils.string2SocketAddress(addr));
                    log.info("begin to connect remote host[{}] asynchronously", addr);
                    remotingChannelFuture = new RemotingChannelFuture(channelFuture);
                    this.channels.put(addr, remotingChannelFuture);
                }
            } catch (Throwable e) {
                log.error("create channel Error.", e);
            } finally {
                this.lock.unlock();
            }
        } else {
            log.warn("try to lock channels, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
        }
        if (remotingChannelFuture != null) {
            ChannelFuture channelFuture = remotingChannelFuture.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(this.configuration.getConnectTimeoutMillis())) {
                if (remotingChannelFuture.isActive()) {
                    log.info("connect remote host[{}] success, {}", addr, channelFuture.toString());
                    return remotingChannelFuture.getChannel();
                } else {
                    log.warn("connect remote host[" + addr + "] Error, " + channelFuture.toString(), channelFuture.cause());
                }
            } else {
                log.warn("connect remote host[{}] timeout {}ms, {}", addr, this.configuration.getConnectTimeoutMillis(), channelFuture.toString());
            }
        }
        return null;
    }

    private static class RemotingChannelFuture {
        private final ChannelFuture channelFuture;
        public RemotingChannelFuture(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        public boolean isActive() {
            return this.channelFuture.channel() != null && this.channelFuture.channel().isActive();
        }

        private Channel getChannel() {
            return this.channelFuture.channel();
        }

        public ChannelFuture getChannelFuture() {
            return channelFuture;
        }
    }

    class NettyClientHandler extends SimpleChannelInboundHandler<RemotingCommand> {
        protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand command) throws Exception {
            commandProcessor(ctx, command);
        }
    }

    class NettyConnectManageHandler extends ChannelDuplexHandler {

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            final String remoteAddress = RemotingUtils.parseChannelRemoteAddr(ctx.channel());
            log.info("NettyRemotingClient pipeline: disconnect {}", remoteAddress);
            NettyRemotingClient.this.close(ctx.channel());
            super.disconnect(ctx, promise);
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            final String remoteAddress = RemotingUtils.parseChannelRemoteAddr(ctx.channel());
            log.info("NettyRemotingClient pipeline: close {}", remoteAddress);
            NettyRemotingClient.this.close(ctx.channel());
            super.close(ctx, promise);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state().equals(IdleState.ALL_IDLE)) {
                    final String remoteAddress = RemotingUtils.parseChannelRemoteAddr(ctx.channel());
                    log.warn("NettyRemotingClient pipeline: idle Error [{}]", remoteAddress);
                    NettyRemotingClient.this.close(ctx.channel());
                }
            }
            ctx.fireUserEventTriggered(evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddress = RemotingUtils.parseChannelRemoteAddr(ctx.channel());
            log.warn("NettyRemotingClient pipeline: exceptionCaught {}", remoteAddress);
            log.warn("NettyRemotingClient pipeline: exceptionCaught Error.", cause);
            NettyRemotingClient.this.close(ctx.channel());
        }
    }
}
