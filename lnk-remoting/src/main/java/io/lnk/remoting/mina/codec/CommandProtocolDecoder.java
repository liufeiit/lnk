package io.lnk.remoting.mina.codec;

import java.nio.ByteBuffer;

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

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.prefixedDataAvailable(RemotingCommand.BODY_LENGTH, Integer.MAX_VALUE - (RemotingCommand.COMMAND_LENGTH_LENGTH - RemotingCommand.BODY_LENGTH))) {
            int bodyLength = in.getInt();
            byte[] commandBytes = new byte[bodyLength];
            in.get(commandBytes);
            RemotingCommand command = this.decodeCommand(commandBytes);
            out.write(command);
            return true;
        }
        return false;
    }

    private RemotingCommand decodeCommand(byte[] commandBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(commandBytes);
        int code = byteBuffer.getInt();
        int version = byteBuffer.getInt();
        int command = byteBuffer.getInt();
        int protocol = byteBuffer.getInt();
        long opaque = byteBuffer.getLong();
        byte[] body = new byte[commandBytes.length - (RemotingCommand.COMMAND_LENGTH_LENGTH - RemotingCommand.BODY_LENGTH)];
        byteBuffer.get(body);
        RemotingCommand remotingCommand = new RemotingCommand();
        remotingCommand.setCode(code);
        remotingCommand.setVersion(version);
        remotingCommand.setCommand(command);
        remotingCommand.setProtocol(protocol);
        remotingCommand.setOpaque(opaque);
        remotingCommand.setBody(body);
        return remotingCommand;
    }
}
