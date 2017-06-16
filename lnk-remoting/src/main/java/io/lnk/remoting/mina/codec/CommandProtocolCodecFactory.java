package io.lnk.remoting.mina.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月15日 上午10:32:24
 */
public class CommandProtocolCodecFactory implements ProtocolCodecFactory {
    private final ProtocolEncoder protocolEncoder;
    private final ProtocolDecoder protocolDecoder;

    public CommandProtocolCodecFactory() {
        super();
        this.protocolEncoder = new CommandProtocolEncoder();
        this.protocolDecoder = new CommandProtocolDecoder();
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        Object protocolEncoder = session.getAttribute("protocol.encoder");
        if (protocolEncoder == null) {
            session.setAttribute("protocol.encoder", this.protocolEncoder);
            return this.protocolEncoder;
        }
        return (ProtocolEncoder) protocolEncoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        Object protocolDecoder = session.getAttribute("protocol.decoder");
        if (protocolDecoder == null) {
            session.setAttribute("protocol.decoder", this.protocolDecoder);
            return this.protocolDecoder;
        }
        return (ProtocolDecoder) protocolDecoder;
    }

}
