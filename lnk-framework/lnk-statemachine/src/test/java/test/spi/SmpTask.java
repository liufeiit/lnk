package test.spi;

import io.statemachine.spi.StateContext;
import io.statemachine.spi.diagram.Task;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月20日 上午9:57:43
 */
public class SmpTask implements Task<States, Events> {

    @Override
    public void execute(StateContext<States, Events> context) throws Throwable {
        System.err.println("context : " + context);
    }

    @Override
    public void complete(StateContext<States, Events> context) {}

}
