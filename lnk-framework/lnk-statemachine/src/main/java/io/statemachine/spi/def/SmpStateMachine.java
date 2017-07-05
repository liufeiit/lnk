package io.statemachine.spi.def;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import io.statemachine.spi.HandlerExceptionResolver;
import io.statemachine.spi.StateMachine;
import io.statemachine.spi.StateMachineExecutor;
import io.statemachine.spi.diagram.Diagram;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午2:52:59
 */
public class SmpStateMachine<S, E> implements StateMachine<S, E> {
    private StateMachineExecutor<S, E> stateMachineExecutor;
    private Map<String, Diagram<S, E>> diagrams;
    private HandlerExceptionResolver<S, E> handlerExceptionResolver;

    @Override
    public void queueEvent(Message<E> message) {
        stateMachineExecutor.queueEvent(message);
    }

    @Override
    public void queueEvent(E event) {
        queueEvent(MessageBuilder.withPayload(event).build());
    }

    @Override
    public boolean sendEvent(Message<E> message) {
        return stateMachineExecutor.executeEvent(message);
    }

    @Override
    public boolean sendEvent(E event) {
        return sendEvent(MessageBuilder.withPayload(event).build());
    }

    @Override
    public Collection<Diagram<S, E>> getDiagrams() {
        return diagrams.values();
    }

    @Override
    public Diagram<S, E> getDiagram(String diagramId) {
        return diagrams.get(diagramId);
    }

    @Override
    public HandlerExceptionResolver<S, E> getHandlerExceptionResolver() {
        return handlerExceptionResolver;
    }
    
    public void setHandlerExceptionResolver(HandlerExceptionResolver<S, E> handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }
    
    public void setStateMachineExecutor(StateMachineExecutor<S, E> stateMachineExecutor) {
        this.stateMachineExecutor = stateMachineExecutor;
    }
    
    public void setDiagrams(Collection<Diagram<S, E>> diagrams) {
        if (diagrams == null || diagrams.isEmpty()) {
            return;
        }
        this.diagrams = new ConcurrentHashMap<String, Diagram<S, E>>();
        for (Diagram<S, E> diagram : diagrams) {
            this.diagrams.put(diagram.getId(), diagram);
        }
    }
}
