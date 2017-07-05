package test.spi;

import io.statemachine.spi.ResourcesStateLoader;
import io.statemachine.spi.StateContext;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月20日 上午9:56:56
 */
public class SmpResourcesStateLoader implements ResourcesStateLoader<States, Events> {

    @Override
    public States getState(StateContext<States, Events> context) throws Throwable {
        return States.WAITING;
    }
}
