package io.lnk.cluster;

import java.util.Random;

import io.lnk.api.Address;
import io.lnk.api.InvokerCommand;
import io.lnk.api.cluster.LoadBalance;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午9:02:00
 */
public class RandomLoadBalance implements LoadBalance {

    @Override
    public Address select(InvokerCommand command, Address[] candidates) {
        int candidatesNum = candidates.length;
        Random random = new Random();
        int randomNum = random.nextInt(candidatesNum);
        return candidates[randomNum];
    }
}
