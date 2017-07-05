package io.statemachine.spi.def;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import io.statemachine.spi.StateContext;
import io.statemachine.spi.StateMachine;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午3:42:56
 */
public class SmpStateContext<S, E> implements StateContext<S, E> {

    private final StateMachine<S, E> stateMachine;

    private final MessageHeaders headers;

    private final Message<E> message;

    private final Map<String, Object> variables;

    private Throwable exception;

    private String diagram;

    private S target;

    private S source;

    private boolean acceptEvent = true;

    public SmpStateContext(StateMachine<S, E> stateMachine, MessageHeaders headers, Message<E> message) {
        super();
        this.stateMachine = stateMachine;
        this.headers = headers;
        this.message = message;
        this.variables = new HashMap<>();
    }

    @Override
    public boolean isAcceptEvent() {
        return acceptEvent;
    }

    @Override
    public Message<E> getMessage() {
        return message;
    }

    @Override
    public E getEvent() {
        return message.getPayload();
    }

    @Override
    public String getDiagram() {
        return diagram;
    }

    @Override
    public MessageHeaders getMessageHeaders() {
        return headers;
    }

    @Override
    public Object getMessageHeader(Object header) {
        return headers.get(header);
    }

    @Override
    public Object getVariable(String key) {
        return variables.get(key);
    }

    @Override
    public void setVariable(String key, Object value) {
        this.setVariable(key, value);
    }

    @Override
    public StateMachine<S, E> getStateMachine() {
        return this.stateMachine;
    }

    @Override
    public S getSource() {
        return source;
    }

    public void setSource(S source) {
        this.source = source;
    }

    @Override
    public S getTarget() {
        return target;
    }

    public void setTarget(S target) {
        this.target = target;
    }

    public void setAcceptEvent(boolean acceptEvent) {
        this.acceptEvent = acceptEvent;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
