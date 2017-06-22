package io.lnk.broker.http;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.api.broker.BrokerCaller;
import io.lnk.api.broker.BrokerConfiguration;
import io.lnk.api.broker.BrokerServer;
import io.lnk.api.utils.LnkThreadFactory;
import io.lnk.remoting.utils.RemotingUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月19日 下午6:54:31
 */
public class HttpBrokerServer implements BrokerServer {
    protected static final Logger log = LoggerFactory.getLogger(HttpBrokerServer.class.getSimpleName());
    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup eventLoopGroupSelector;
    private final EventLoopGroup eventLoopGroupBoss;
    private final BrokerConfiguration configuration;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private Channel serverChannel;
    private InetSocketAddress serverAddress;
    private BrokerCaller caller;

    public HttpBrokerServer(final BrokerConfiguration configuration) {
        this.serverBootstrap = new ServerBootstrap();
        this.configuration = configuration;
        this.eventLoopGroupBoss = new NioEventLoopGroup(2, LnkThreadFactory.newThreadFactory("HttpBrokerServerBoss-%d", false));
        int serverSelectorThreads = configuration.getSelectorThreads();
        if (RemotingUtils.isLinuxPlatform() && configuration.isUseEpollNativeSelector()) {
            this.eventLoopGroupSelector = new EpollEventLoopGroup(serverSelectorThreads, LnkThreadFactory.newThreadFactory("HttpBrokerServerEPOLLSelector-" + serverSelectorThreads + "-%d", false));
        } else {
            this.eventLoopGroupSelector = new NioEventLoopGroup(serverSelectorThreads, LnkThreadFactory.newThreadFactory("HttpBrokerServerNIOSelector-" + serverSelectorThreads + "-%d", false));
        }
    }

    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(configuration.getWorkerThreads(), LnkThreadFactory.newThreadFactory("HttpBrokerServerCodecThread-%d", false));
        ServerBootstrap childHandler = this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_SNDBUF, this.configuration.getSocketSndBufSize())
                .option(ChannelOption.SO_RCVBUF, this.configuration.getSocketRcvBufSize())
                .localAddress(new InetSocketAddress(this.configuration.getListenPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(defaultEventExecutorGroup, 
                                new HttpServerCodec(), new HttpObjectAggregator(1024 * 1024 * 100),
                                new HttpIoHandler(caller));
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

    public Channel getServerChannel() {
        return this.serverChannel;
    }

    public InetSocketAddress getServerAddress() {
        return this.serverAddress;
    }

    public void shutdown() {
        try {
            this.eventLoopGroupBoss.shutdownGracefully();
            this.eventLoopGroupSelector.shutdownGracefully();
            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Throwable e) {
            log.error("HttpBrokerServer shutdown Error.", e);
        }
    }

    @Override
    public void setBrokerCaller(BrokerCaller caller) {
        this.caller = caller;
    }
}
