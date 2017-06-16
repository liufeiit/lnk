package io.lnk.remoting.protocol;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月19日 下午2:05:11
 */
public interface CommandCode {
    int SUCCESS = 0;
    int SYSTEM_ERROR = 1;
    int SYSTEM_BUSY = 2;
    int COMMAND_CODE_NOT_SUPPORTED = 3;
}
