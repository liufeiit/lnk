package io.statemachine.spi.diagram.def;

import io.statemachine.spi.diagram.State;
import io.statemachine.spi.diagram.Task;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午5:01:01
 */
public class SmpState<S, E> implements State<S, E> {
    
    private E event;
    
    private S source;
    
    private S target;
    
    private Task<S, E> task;

    @Override
    public E getEvent() {
        return event;
    }

    @Override
    public S getSource() {
        return source;
    }

    @Override
    public S getTarget() {
        return target;
    }

    @Override
    public Task<S, E> getTask() {
        return task;
    }
    
    public void setEvent(E event) {
        this.event = event;
    }
    
    public void setSource(S source) {
        this.source = source;
    }
    
    public void setTarget(S target) {
        this.target = target;
    }
    
    public void setTask(Task<S, E> task) {
        this.task = task;
    }
}
