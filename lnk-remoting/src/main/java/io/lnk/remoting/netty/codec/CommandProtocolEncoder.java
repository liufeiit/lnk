package io.lnk.remoting.netty.codec;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.remoting.protocol.RemotingCommand;
import io.lnk.remoting.utils.RemotingUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月18日 上午11:57:58
 */
public class CommandProtocolEncoder extends MessageToByteEncoder<RemotingCommand> {
    
    private static final Logger log = LoggerFactory.getLogger(CommandProtocolEncoder.class.getSimpleName());

    @Override
    protected void encode(ChannelHandlerContext ctx, RemotingCommand command, ByteBuf out) throws Exception {
        try {
            ByteBuffer header = this.encodeHeader(command);
            out.writeBytes(header);
            out.writeBytes(command.getBody());
        } catch (Throwable e) {
            log.error("encode RemotingCommand to ByteBuf Error, RemoteAddr : " + RemotingUtils.parseChannelRemoteAddr(ctx.channel()), e);
            RemotingUtils.closeChannel(ctx.channel());
        }
    }
    
    public ByteBuffer encodeHeader(RemotingCommand command) {
        ByteBuffer headerBytes = ByteBuffer.allocate(RemotingCommand.COMMAND_LENGTH_LENGTH);
        headerBytes.putInt(command.getCode());
        headerBytes.putInt(command.getVersion());
        headerBytes.putInt(command.getCommand());
        headerBytes.putInt(command.getProtocol());
        headerBytes.putLong(command.getOpaque());
        headerBytes.putInt(command.getBody().length);
        headerBytes.flip();
        return headerBytes;
    }
}
