package io.lnk.remoting.mina.codec;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import io.lnk.remoting.protocol.RemotingCommand;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月15日 上午10:12:05
 */
public class CommandProtocolEncoder extends ProtocolEncoderAdapter {

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        RemotingCommand command = (RemotingCommand) message;
        byte[] body = this.encode(command);
        IoBuffer buf = IoBuffer.allocate(4 + body.length).setAutoExpand(true);
        buf.putInt(body.length);
        buf.put(body);
        buf.flip();
        out.write(buf);
    }

    private byte[] encode(RemotingCommand command) {
        int bodyLength = 0;
        byte[] body = command.getBody();
        if (body != null) {
            bodyLength = body.length;
        }
        ByteBuffer commandBytes = ByteBuffer.allocate(RemotingCommand.COMMAND_LENGTH_LENGTH + bodyLength);
        commandBytes.putInt(command.getCode());
        commandBytes.putInt(command.getVersion());
        commandBytes.putInt(command.getCommand());
        commandBytes.putInt(command.getProtocol());
        commandBytes.putLong(command.getOpaque());
        if (body != null) {
            commandBytes.put(body);
        }
        commandBytes.flip();
        return commandBytes.array();
    }
}
