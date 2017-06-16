package io.lnk.remoting.mina.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.remoting.protocol.RemotingCommand;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月15日 上午10:12:05
 */
public class CommandProtocolEncoder implements ProtocolEncoder {
    private static final Logger log = LoggerFactory.getLogger(CommandProtocolEncoder.class.getSimpleName());

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        try {
            RemotingCommand command = (RemotingCommand) message;
            byte[] header = command.encodeHeader().array();
            byte[] body = command.getBody();
            IoBuffer buf = IoBuffer.allocate(header.length + body.length).setAutoExpand(true);
            buf.put(header);
            buf.put(body);
            buf.flip();
            out.write(buf);
        } catch (Throwable e) {
            log.error("encode " + message + " Error.", e);
            session.close(false);
        }
    }

    @Override
    public void dispose(IoSession session) throws Exception {}
}
