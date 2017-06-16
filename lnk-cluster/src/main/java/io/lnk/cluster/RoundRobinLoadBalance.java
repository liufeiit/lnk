package io.lnk.cluster;

import io.lnk.api.Address;
import io.lnk.api.InvokerCommand;
import io.lnk.api.cluster.LoadBalance;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午9:05:46
 */
public class RoundRobinLoadBalance implements LoadBalance {
    private static Integer index = 0;

    @Override
    public Address select(InvokerCommand command, Address[] candidates) {
        Address candidate;
        synchronized (index) {
            if (index >= candidates.length) {
                index = 0;
            }
            candidate = candidates[index];
            index++;
        }
        return candidate;
    }
}
