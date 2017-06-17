package io.lnk.remoting.mina;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.service.IoServiceStatistics;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.remoting.CommandProcessor;
import io.lnk.remoting.Pair;
import io.lnk.remoting.RemotingCallback;
import io.lnk.remoting.RemotingServer;
import io.lnk.remoting.ServerConfiguration;
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
 * @since 2017年5月19日 下午6:54:31
 */
public class MinaRemotingServer extends MinaAbstractRemotingService implements RemotingServer {
    private final NioSocketAcceptor acceptor;
    private final ServerConfiguration configuration;
    private final ExecutorService defaultThreadPoolExecutor;
    private InetSocketAddress serverAddress;

    public MinaRemotingServer(final ProtocolFactorySelector protocolFactorySelector, final ServerConfiguration configuration) {
        super(protocolFactorySelector);
        this.configuration = configuration;
        IoBuffer.setUseDirectBuffer(false);
        this.defaultThreadPoolExecutor =
                Executors.newFixedThreadPool(configuration.getDefaultExecutorThreads(), RemotingThreadFactory.newThreadFactory("MinaRemotingServerDefaultThreadPoolExecutor-%d", false));
        this.acceptor = new NioSocketAcceptor(configuration.getWorkerThreads());
        this.acceptor.getFilterChain().addLast("exceutor", new ExecutorFilter(
                Executors.newFixedThreadPool(configuration.getDefaultExecutorThreads(), RemotingThreadFactory.newThreadFactory("MinaRemotingServerDefaultThreadPoolExecutor-%d", false))));
        this.acceptor.getFilterChain().addLast("mdc", new MdcInjectionFilter());
        this.acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        this.acceptor.getFilterChain().addLast("codec", new CommandProtocolCodecFilter());
        this.acceptor.setReuseAddress(true);
        this.acceptor.setBacklog(1024);
        this.acceptor.getSessionConfig().setReuseAddress(true);
        this.acceptor.getSessionConfig().setReadBufferSize(configuration.getSocketRcvBufSize());
        this.acceptor.getSessionConfig().setReceiveBufferSize(configuration.getSocketRcvBufSize());
        this.acceptor.getSessionConfig().setSendBufferSize(configuration.getSocketSndBufSize());
        this.acceptor.getSessionConfig().setTcpNoDelay(true);
        this.acceptor.getSessionConfig().setSoLinger(-1);
        this.acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, configuration.getChannelMaxIdleTimeSeconds());
    }

    @Override
    public void start() {
        try {
            if (this.configuration.isPooledByteBufAllocatorEnable()) {
                IoBuffer.setAllocator(new SimpleBufferAllocator());
            }
            this.acceptor.setHandler(new IoServerHandler());
            InetSocketAddress serverAddress = new InetSocketAddress(this.configuration.getListenPort());
            this.acceptor.bind(serverAddress);
            this.serverAddress = serverAddress;
        } catch (Throwable e) {
            throw new RuntimeException("NioSocketAcceptor.bind Error.", e);
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

    @Override
    public InetSocketAddress getServerAddress() {
        return this.serverAddress;
    }

    @Override
    public Pair<CommandProcessor, ExecutorService> getProcessorPair(int commandCode) {
        return this.processors.get(commandCode);
    }

    public RemotingCommand invokeSync(IoSession session, RemotingCommand request, long timeoutMillis) throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException {
        return this.__invokeSync(session, request, timeoutMillis);
    }

    public void invokeAsync(IoSession session, RemotingCommand request, long timeoutMillis, RemotingCallback callback)
            throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException {
        this.__invokeAsync(session, request, timeoutMillis, callback);
    }

    public void invokeOneway(IoSession session, RemotingCommand request) throws InterruptedException, RemotingSendRequestException {
        this.__invokeOneway(session, request);
    }

    @Override
    public void shutdown() {
        try {
            IoServiceStatistics statistics = this.acceptor.getStatistics();  
            statistics.updateThroughput(System.currentTimeMillis());  
            System.out.println(String.format("total read bytes: %d, read throughtput: %f (b/s)", new Object[] { Long.valueOf(statistics.getReadBytes()), Double.valueOf(statistics.getReadBytesThroughput()) }));  
            System.out.println(String.format("total read msgs: %d, read msg throughtput: %f (msg/s)", new Object[] { Long.valueOf(statistics.getReadMessages()), Double.valueOf(statistics.getReadMessagesThroughput()) }));  
            for (IoSession session : this.acceptor.getManagedSessions().values()) {  
              if ((session.isConnected()) && (!session.isClosing())) {  
                session.closeOnFlush();  
              }  
            }  
            this.acceptor.unbind();
            this.acceptor.dispose();
        } catch (Throwable e) {
            log.error("MinaRemotingServer shutdown Error.", e);
        }
        if (this.defaultThreadPoolExecutor != null) {
            try {
                this.defaultThreadPoolExecutor.shutdown();
            } catch (Throwable e) {
                log.error("MinaRemotingServer shutdown Error.", e);
            }
        }
    }

    class IoServerHandler extends IoHandlerAdapter {
        public void messageReceived(IoSession session, Object message) throws Exception {
            commandProcessor(session, (RemotingCommand) message);
        }

        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            final String remoteAddress = RemotingUtils.parseSessionRemoteAddr(session);
            log.warn("MinaRemotingServer pipeline: exceptionCaught {}", remoteAddress);
            log.warn("MinaRemotingServer pipeline: exceptionCaught Error.", cause);
            session.closeOnFlush();
        }

        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
            if (status == IdleStatus.BOTH_IDLE) {
                session.closeOnFlush();
            }
        }
    }
}
