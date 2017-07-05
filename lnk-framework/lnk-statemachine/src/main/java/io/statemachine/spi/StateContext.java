package io.statemachine.spi;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午2:58:40
 */
public interface StateContext<S, E> {

    Message<E> getMessage();

    E getEvent();
    
    String getDiagram();

    MessageHeaders getMessageHeaders();
    
    Object getMessageHeader(Object header);
    
    Object getVariable(String key);
    
    void setVariable(String key, Object value);
    
    StateMachine<S, E> getStateMachine();
    
    boolean isAcceptEvent();
    
    S getSource();
    
    S getTarget();
    
    Throwable getException();
}
