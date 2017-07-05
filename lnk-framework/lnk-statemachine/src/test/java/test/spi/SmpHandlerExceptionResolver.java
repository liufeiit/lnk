package test.spi;

import org.springframework.messaging.Message;

import io.statemachine.spi.HandlerExceptionResolver;
import io.statemachine.spi.StateContext;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月20日 上午9:55:02
 */
public class SmpHandlerExceptionResolver implements HandlerExceptionResolver<States, Events> {

    @Override
    public void handle(Message<Events> message, StateContext<States, Events> context, Throwable t) {
        System.err.println("message : " + message);
        System.err.println("context : " + context);
        t.printStackTrace(System.err);
    }
}
