package io.lnk.remoting.mina.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import io.lnk.remoting.protocol.RemotingCommand;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月15日 上午10:12:44
 */
public class CommandProtocolDecoder extends CumulativeProtocolDecoder {

    /**
     * true if and only if there's more to decode in the buffer and you want to have doDecode method invoked again.
     * Return false if remaining data is not enough to decode, then this method will be invoked again when more data is
     * cumulated.
     */
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.remaining() < RemotingCommand.COMMAND_HEADER_LENGTH) {
            return false;
        }
        if (in.remaining() > RemotingCommand.COMMAND_HEADER_LENGTH) {
            in.mark();
            int code = in.getInt();
            int version = in.getInt();
            int command = in.getInt();
            int protocol = in.getInt();
            long opaque = in.getLong();
            int bodyLength = in.getInt();
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
            return true;
        }
        return false;
    }
}
