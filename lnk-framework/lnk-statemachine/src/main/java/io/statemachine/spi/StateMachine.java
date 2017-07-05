package io.statemachine.spi;

import java.util.Collection;

import org.springframework.messaging.Message;

import io.statemachine.spi.diagram.Diagram;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午2:27:29
 */
public interface StateMachine<S, E> {
    
    String MESSAGE_DIAGRAM_HEADER_ID = "_sm.mdh.id_";
    
    void queueEvent(Message<E> message);
    
    void queueEvent(E event);
    
    boolean sendEvent(Message<E> message);

    boolean sendEvent(E event);
    
    Collection<Diagram<S, E>> getDiagrams();
    
    Diagram<S, E> getDiagram(String diagramId);
    
    HandlerExceptionResolver<S, E> getHandlerExceptionResolver();
}
