package io.lnk.api.protocol;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午4:50:40
 */
public interface ProtocolFactorySelector {
    ProtocolFactory select(int protocol);
}
