package io.statemachine.spi.def;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.StringUtils;

import io.statemachine.spi.HandlerExceptionResolver;
import io.statemachine.spi.ResourcesStateLoader;
import io.statemachine.spi.StateMachine;
import io.statemachine.spi.StateMachineExecutor;
import io.statemachine.spi.diagram.Diagram;
import io.statemachine.spi.diagram.State;
import io.statemachine.spi.diagram.Task;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午3:28:22
 */
public class SmpStateMachineExecutor<S, E> implements StateMachineExecutor<S, E> {
    private static final Logger log = LoggerFactory.getLogger(SmpStateMachineExecutor.class.getSimpleName());
    private StateMachine<S, E> stateMachine;
    private AsyncTaskExecutor asyncTaskExecutor;
    
    private class AsyncEventTask implements Runnable {
        private Message<E> message;

        public AsyncEventTask(Message<E> message) {
            super();
            this.message = message;
        }

        @Override
        public void run() {
            boolean executeEvent = SmpStateMachineExecutor.this.executeEvent(message);
            log.info("SmpStateMachineExecutor#AsyncEventTask#run executeEvent-{} message : {}.", executeEvent, message);
        }
    }

    @Override
    public void queueEvent(Message<E> message) {
        asyncTaskExecutor.submit(new AsyncEventTask(message));
    }
    
    private boolean executeEvent(Message<E> message, SmpStateContext<S, E> context, Diagram<S, E> diagram) {
        HandlerExceptionResolver<S, E> handlerExceptionResolver = stateMachine.getHandlerExceptionResolver();
        ResourcesStateLoader<S, E> loader = diagram.getStateLoader();
        State<S, E> state = diagram.getState(message.getPayload());
        if (state == null) {
            context.setAcceptEvent(false);
            log.info("SmpStateMachineExecutor#executeEvent Not Accept Event : {}", message.getPayload());
            return false;
        }
        S source = state.getSource();
        context.setSource(source);
        context.setTarget(state.getTarget());
        S resourcesState = null;
        try {
            resourcesState = loader.getState(context);
            if (source.equals(resourcesState) == false) {
                context.setAcceptEvent(false);
                log.info("SmpStateMachineExecutor#executeEvent Not Accept resourcesState : {} for Event : {}", resourcesState, message.getPayload());
                return false;
            }
        } catch (Throwable e) {
            context.setException(e);
            log.error("SmpStateMachineExecutor#executeEvent@ResourcesStateLoader#getState Error.", e);
            handlerExceptionResolver.handle(message, context, e);
            throw new IllegalStateException("SmpStateMachineExecutor#executeEvent@ResourcesStateLoader#getState Error.", e);
        }
        try {
            Task<S, E> task = state.getTask();
            task.execute(context);
            task.complete(context);
            resourcesState = loader.getState(context);
            return state.getTarget().equals(resourcesState);
        } catch (Throwable e) {
            context.setException(e);
            log.error("SmpStateMachineExecutor#executeEvent@Task#execute Error.", e);
            handlerExceptionResolver.handle(message, context, e);
            throw new IllegalStateException("SmpStateMachineExecutor#executeEvent@Task#execute Error.", e);
        }
    }

    @Override
    public boolean executeEvent(Message<E> message) {
        SmpStateContext<S, E> context = buildStateContext(message, stateMachine);
        String diagramId = message.getHeaders().get(StateMachine.MESSAGE_DIAGRAM_HEADER_ID, String.class);
        if (StringUtils.hasText(diagramId)) {
            Diagram<S, E> diagram = stateMachine.getDiagram(diagramId);
            return executeEvent(message, context, diagram);
        }
        Collection<Diagram<S, E>> diagrams = stateMachine.getDiagrams();
        boolean executeEvent = false;
        for (Diagram<S, E> diagram : diagrams) {
            try {
                executeEvent = (executeEvent || executeEvent(message, context, diagram));
            } catch (Throwable e) {
                log.error("SmpStateMachineExecutor#executeEvent foreach Error.", e);
            }
        }
        return executeEvent;
    }

    private SmpStateContext<S, E> buildStateContext(Message<E> message, StateMachine<S, E> stateMachine) {
        MessageHeaders messageHeaders = ((message != null) ? message.getHeaders() : new MessageHeaders(new HashMap<String, Object>()));
        Map<String, Object> headersData = new HashMap<String, Object>(messageHeaders);
        return new SmpStateContext<S, E>(stateMachine, new MessageHeaders(headersData), message);
    }

    public void setAsyncTaskExecutor(AsyncTaskExecutor asyncTaskExecutor) {
        this.asyncTaskExecutor = asyncTaskExecutor;
    }
    
    public void setStateMachine(StateMachine<S, E> stateMachine) {
        this.stateMachine = stateMachine;
    }
}
