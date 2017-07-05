package io.statemachine.spi.diagram;

import io.statemachine.spi.StateContext;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午3:18:19
 */
public interface Task<S, E> {
    void execute(StateContext<S, E> context) throws Throwable;
    void complete(StateContext<S, E> context);
}
