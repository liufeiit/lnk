package io.statemachine.spi;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午3:06:45
 */
public interface ResourcesStateLoader<S, E> {
    S getState(StateContext<S, E> context) throws Throwable;
}
