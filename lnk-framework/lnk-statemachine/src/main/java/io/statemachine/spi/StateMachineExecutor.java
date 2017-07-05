package io.statemachine.spi;

import org.springframework.messaging.Message;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午3:22:21
 */
public interface StateMachineExecutor<S, E> {
    void queueEvent(Message<E> message);
    boolean executeEvent(Message<E> message);
}
