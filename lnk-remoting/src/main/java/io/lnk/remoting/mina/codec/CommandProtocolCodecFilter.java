package io.lnk.remoting.mina.codec;

import org.apache.mina.filter.codec.ProtocolCodecFilter;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月15日 上午10:34:44
 */
public class CommandProtocolCodecFilter extends ProtocolCodecFilter {

    public CommandProtocolCodecFilter() {
        super(new CommandProtocolCodecFactory());
    }
}
