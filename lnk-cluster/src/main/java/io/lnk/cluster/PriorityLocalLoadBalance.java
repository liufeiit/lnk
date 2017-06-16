package io.lnk.cluster;

import org.apache.commons.lang3.StringUtils;

import io.lnk.api.Address;
import io.lnk.api.InvokerCommand;
import io.lnk.api.cluster.LoadBalance;
import io.lnk.remoting.utils.RemotingUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月26日 下午2:16:21
 */
public class PriorityLocalLoadBalance implements LoadBalance {
    private final String ip;
    private final LoadBalance loadBalance;
    
    public PriorityLocalLoadBalance() {
        this.ip = RemotingUtils.getLocalAddress();
        this.loadBalance = new RandomLoadBalance();
    }

    @Override
    public Address select(InvokerCommand command, Address[] candidates) {
        for (Address candidate : candidates) {
            if (StringUtils.startsWith(candidate.toString(), ip)) {
                return candidate;
            }
        }
        return loadBalance.select(command, candidates);
    }
}
