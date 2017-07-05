package io.statemachine.spi;

import org.springframework.messaging.Message;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午4:24:42
 */
public interface HandlerExceptionResolver<S, E> {
    void handle(Message<E> message, StateContext<S, E> context, Throwable t);
}
