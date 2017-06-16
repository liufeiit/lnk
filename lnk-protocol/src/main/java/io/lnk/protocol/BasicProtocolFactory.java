package io.lnk.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.api.protocol.ProtocolFactory;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月2日 下午8:54:27
 */
public abstract class BasicProtocolFactory implements ProtocolFactory {
    protected final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private final int protocol;

    protected BasicProtocolFactory(int protocol) {
        super();
        this.protocol = protocol;
    }

    @Override
    public final int getProtocol() {
        return this.protocol;
    }
}