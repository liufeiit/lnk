package io.lnk.remoting;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月18日 下午9:39:14
 */
public interface RemotingServer extends RemotingService {

    Pair<CommandProcessor, ExecutorService> getProcessorPair(int commandCode);

    void registerProcessor(int commandCode, CommandProcessor processor, ExecutorService executor);

    void registerDefaultProcessor(CommandProcessor processor, ExecutorService executor);
    
    InetSocketAddress getServerAddress();
}
