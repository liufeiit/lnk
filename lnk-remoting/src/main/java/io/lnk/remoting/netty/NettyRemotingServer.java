package io.lnk.remoting.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.remoting.CommandProcessor;
import io.lnk.remoting.ServerConfiguration;
import io.lnk.remoting.Pair;
import io.lnk.remoting.RemotingCallback;
import io.lnk.remoting.RemotingServer;
import io.lnk.remoting.exception.RemotingSendRequestException;
import io.lnk.remoting.exception.RemotingTimeoutException;
import io.lnk.remoting.netty.codec.CommandProtocolDecoder;
import io.lnk.remoting.netty.codec.CommandProtocolEncoder;
import io.lnk.remoting.protocol.RemotingCommand;
import io.lnk.remoting.utils.RemotingThreadFactory;
import io.lnk.remoting.utils.RemotingUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月19日 下午6:54:31
 */
public class NettyRemotingServer extends NettyAbstractRemotingService implements RemotingServer {
    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup eventLoopGroupSelector;
    private final EventLoopGroup eventLoopGroupBoss;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private Channel serverChannel;
    private InetSocketAddress serverAddress;
    private final ServerConfiguration configuration;
    private final ExecutorService defaultThreadPoolExecutor;

    public NettyRemotingServer(final ProtocolFactorySelector protocolFactorySelector, final ServerConfiguration configuration) {
        super(protocolFactorySelector);
        this.serverBootstrap = new ServerBootstrap();
        this.configuration = configuration;
        this.defaultThreadPoolExecutor = Executors.newFixedThreadPool(configuration.getDefaultExecutorThreads(), RemotingThreadFactory.newThreadFactory("NettyRemotingServerDefaultThreadPoolExecutor-%d", false));
        this.eventLoopGroupBoss = new NioEventLoopGroup(2, RemotingThreadFactory.newThreadFactory("NettyRemotingServerBoss-%d", false));
        int serverSelectorThreads = configuration.getSelectorThreads();
        if (RemotingUtils.isLinuxPlatform() && configuration.isUseEpollNativeSelector()) {
            this.eventLoopGroupSelector = new EpollEventLoopGroup(serverSelectorThreads, RemotingThreadFactory.newThreadFactory("NettyRemotingServerEPOLLSelector-" + serverSelectorThreads + "-%d", false));
        } else {
            this.eventLoopGroupSelector = new NioEventLoopGroup(serverSelectorThreads, RemotingThreadFactory.newThreadFactory("NettyRemotingServerNIOSelector-" + serverSelectorThreads + "-%d", false));
        }
    }

    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(configuration.getWorkerThreads(), RemotingThreadFactory.newThreadFactory("NettyRemotingServerCodecThread-%d", false));
        ServerBootstrap childHandler = this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_SNDBUF, this.configuration.getSocketSndBufSize())
                .option(ChannelOption.SO_RCVBUF, this.configuration.getSocketRcvBufSize())
                .localAddress(new InetSocketAddress(this.configuration.getListenPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(defaultEventExecutorGroup, new CommandProtocolEncoder(), new CommandProtocolDecoder(),
                                new IdleStateHandler(0, 0, configuration.getChannelMaxIdleTimeSeconds()), 
                                new NettyConnectManageHandler(), new NettyServerHandler());
                    }
                });
        if (configuration.isPooledByteBufAllocatorEnable()) {
            childHandler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }
        try {
            ChannelFuture sync = this.serverBootstrap.bind().sync();
            this.serverChannel = sync.channel();
            this.serverAddress = (InetSocketAddress) this.serverChannel.localAddress();
        } catch (InterruptedException ex) {
            throw new RuntimeException("ServerBootstrap.bind().sync() InterruptedException", ex);
        }
    }

    @Override
    public void registerProcessor(int commandCode, CommandProcessor processor, ExecutorService executor) {
        ExecutorService executorPair = (executor == null) ? this.defaultThreadPoolExecutor : executor;
        Pair<CommandProcessor, ExecutorService> pair = new Pair<CommandProcessor, ExecutorService>(processor, executorPair);
        this.processors.put(commandCode, pair);
    }

    @Override
    public void registerDefaultProcessor(CommandProcessor processor, ExecutorService executor) {
        ExecutorService executorPair = (executor == null) ? this.defaultThreadPoolExecutor : executor;
        this.defaultCommandProcessor = new Pair<CommandProcessor, ExecutorService>(processor, executorPair);
    }

    @Override
    protected ExecutorService getCallbackExecutor() {
        return this.defaultThreadPoolExecutor;
    }

    public Channel getServerChannel() {
        return this.serverChannel;
    }

    @Override
    public InetSocketAddress getServerAddress() {
        return this.serverAddress;
    }

    @Override
    public Pair<CommandProcessor, ExecutorService> getProcessorPair(int commandCode) {
        return this.processors.get(commandCode);
    }

    public RemotingCommand invokeSync(Channel channel, RemotingCommand request, long timeoutMillis) throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException {
        return this.__invokeSync(channel, request, timeoutMillis);
    }

    public void invokeAsync(Channel channel, RemotingCommand request, long timeoutMillis, RemotingCallback callback) throws RemotingSendRequestException {
        this.__invokeAsync(channel, request, timeoutMillis, callback);
    }

    public void invokeOneway(Channel channel, RemotingCommand request) throws RemotingSendRequestException {
        this.__invokeOneway(channel, request);
    }

    @Override
    public void shutdown() {
        try {
            this.eventLoopGroupBoss.shutdownGracefully();
            this.eventLoopGroupSelector.shutdownGracefully();
            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Throwable e) {
            log.error("NettyRemotingServer shutdown Error.", e);
        }
        if (this.defaultThreadPoolExecutor != null) {
            try {
                this.defaultThreadPoolExecutor.shutdown();
            } catch (Throwable e) {
                log.error("NettyRemotingServer shutdown Error.", e);
            }
        }
    }

    class NettyServerHandler extends SimpleChannelInboundHandler<RemotingCommand> {
        protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand command) throws Exception {
            commandProcessor(ctx, command);
        }
    }

    class NettyConnectManageHandler extends ChannelDuplexHandler {

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state().equals(IdleState.ALL_IDLE)) {
                    RemotingUtils.closeChannel(ctx.channel());
                }
            }
            ctx.fireUserEventTriggered(evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddress = RemotingUtils.parseChannelRemoteAddr(ctx.channel());
            log.warn("NettyRemotingServer pipeline: exceptionCaught {}", remoteAddress);
            log.warn("NettyRemotingServer pipeline: exceptionCaught Error.", cause);
            RemotingUtils.closeChannel(ctx.channel());
        }
    }
}
