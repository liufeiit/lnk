package io.lnk.remoting.netty.codec;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.remoting.SystemConfiguration;
import io.lnk.remoting.protocol.RemotingCommand;
import io.lnk.remoting.utils.RemotingUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月18日 上午11:57:41
 */
public class CommandProtocolDecoder extends LengthFieldBasedFrameDecoder {
    private static final Logger log = LoggerFactory.getLogger(CommandProtocolDecoder.class.getSimpleName());
    private static final int FRAME_MAX_LENGTH = Integer.getInteger(SystemConfiguration.IO_REMOTING_FRAME_MAXLENGTH, (Integer.MAX_VALUE - RemotingCommand.COMMAND_LENGTH_LENGTH));

    public CommandProtocolDecoder() {
        super(FRAME_MAX_LENGTH, (RemotingCommand.COMMAND_LENGTH_LENGTH - RemotingCommand.BODY_LENGTH), RemotingCommand.BODY_LENGTH, 0, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (frame == null) {
                return null;
            }
            return this.decodeCommand(frame.nioBuffer());
        } catch (Throwable e) {
            log.error("decode ByteBuf to RemotingCommand Error, RemoteAddr : " + RemotingUtils.parseChannelRemoteAddr(ctx.channel()), e);
            RemotingUtils.closeChannel(ctx.channel());
        } finally {
            if (frame != null) {
                frame.release();
            }
        }
        return null;
    }
    
    private RemotingCommand decodeCommand(ByteBuffer byteBuffer) {
        int code = byteBuffer.getInt();
        int version = byteBuffer.getInt();
        int command = byteBuffer.getInt();
        int protocol = byteBuffer.getInt();
        long opaque = byteBuffer.getLong();
        int bodyLength = byteBuffer.getInt();
        RemotingCommand remotingCommand = new RemotingCommand();
        remotingCommand.setCode(code);
        remotingCommand.setVersion(version);
        remotingCommand.setCommand(command);
        remotingCommand.setProtocol(protocol);
        remotingCommand.setOpaque(opaque);
        byte[] body = new byte[bodyLength];
        byteBuffer.get(body);
        remotingCommand.setBody(body);
        return remotingCommand;
    }
}
