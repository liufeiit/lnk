package io.lnk.remoting.mina.codec;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.remoting.protocol.RemotingCommand;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月15日 上午10:12:44
 */
public class CommandProtocolDecoder extends CumulativeProtocolDecoder {
    private static final Logger log = LoggerFactory.getLogger(CommandProtocolDecoder.class.getSimpleName());

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.remaining() < RemotingCommand.COMMAND_HEADER_LENGTH) {
            return false;
        }
        if (in.remaining() > 0) {
            try {
                byte[] head = new byte[RemotingCommand.COMMAND_HEADER_LENGTH];
                in.mark();
                in.get(head);
                ByteBuffer headBuffer = ByteBuffer.wrap(head);
                int code = headBuffer.getInt();
                int version = headBuffer.getInt();
                int command = headBuffer.getInt();
                int protocol = headBuffer.getInt();
                long opaque = headBuffer.getLong();
                int bodyLength = headBuffer.getInt();
                if (bodyLength > in.remaining()) {
                    in.reset();
                    return false;
                }
                byte[] body = new byte[bodyLength];
                in.get(body);
                RemotingCommand remotingCommand = new RemotingCommand();
                remotingCommand.setCode(code);
                remotingCommand.setVersion(version);
                remotingCommand.setCommand(command);
                remotingCommand.setProtocol(protocol);
                remotingCommand.setOpaque(opaque);
                remotingCommand.setBody(body);
                out.write(remotingCommand);
                if (in.remaining() > 0) {
                    return true;
                }
            } catch (Throwable e) {
                log.error("doDecode Error.", e);
                session.close(false);
            }
        }
        return false;
    }
}
