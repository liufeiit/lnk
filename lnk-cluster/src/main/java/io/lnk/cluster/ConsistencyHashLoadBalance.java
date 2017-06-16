package io.lnk.cluster;

import io.lnk.api.Address;
import io.lnk.api.InvokerCommand;
import io.lnk.api.cluster.LoadBalance;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午9:01:34
 */
public class ConsistencyHashLoadBalance implements LoadBalance {

    @Override
    public Address select(InvokerCommand command, Address[] candidates) {
        int index = Math.abs(command.getId().hashCode()) % candidates.length;
        return candidates[index];
    }
}
