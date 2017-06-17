package io.lnk.remoting.mina.codec;

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
        byte[] header = command.encodeHeader().array();
        byte[] body = command.getBody();
        IoBuffer buf = IoBuffer.allocate(header.length + body.length).setAutoExpand(true);
        buf.put(header);
        buf.put(body);
        buf.flip();
        out.write(buf);
    }
}
