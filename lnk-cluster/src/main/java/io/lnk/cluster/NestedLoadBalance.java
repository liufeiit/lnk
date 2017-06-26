package io.lnk.cluster;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.api.Address;
import io.lnk.api.InvokerCommand;
import io.lnk.api.cluster.LoadBalance;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月26日 下午6:01:21
 */
public class NestedLoadBalance implements LoadBalance {
    private static final Logger log = LoggerFactory.getLogger(NestedLoadBalance.class.getSimpleName());
    private final LoadBalance nestedLoadBalance;

    public NestedLoadBalance(String type) {
        super();
        log.info("nested load balance type : {}", type);
        if (StringUtils.equals(type, "hash")) {
            this.nestedLoadBalance = new ConsistencyHashLoadBalance();
        } else if (StringUtils.equals(type, "random")) {
            this.nestedLoadBalance = new RandomLoadBalance();
        } else if (StringUtils.equals(type, "roundrobin")) {
            this.nestedLoadBalance = new RoundRobinLoadBalance();
        } else if (StringUtils.equals(type, "local")) {
            this.nestedLoadBalance = new PriorityLocalLoadBalance();
        } else {
            this.nestedLoadBalance = new ConsistencyHashLoadBalance();
        }
    }

    @Override
    public Address select(InvokerCommand command, Address[] candidates) {
        return nestedLoadBalance.select(command, candidates);
    }
}
