package io.lnk.api.cluster;

import io.lnk.api.Address;
import io.lnk.api.InvokerCommand;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午2:42:57
 */
public interface LoadBalance {
    Address select(InvokerCommand command, Address[] candidates);
}