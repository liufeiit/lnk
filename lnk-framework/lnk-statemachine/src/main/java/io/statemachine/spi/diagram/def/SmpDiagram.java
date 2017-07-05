package io.statemachine.spi.diagram.def;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.statemachine.spi.ResourcesStateLoader;
import io.statemachine.spi.diagram.Diagram;
import io.statemachine.spi.diagram.State;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午5:00:06
 */
public class SmpDiagram<S, E> implements Diagram<S, E> {

    private String id;
    
    private Map<E, State<S, E>> states;
    
    private ResourcesStateLoader<S, E> stateLoader;
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public Collection<State<S, E>> getStates() {
        return states.values();
    }

    @Override
    public State<S, E> getState(E event) {
        return states.get(event);
    }

    @Override
    public ResourcesStateLoader<S, E> getStateLoader() {
        return stateLoader;
    }
    
    public void setStateLoader(ResourcesStateLoader<S, E> stateLoader) {
        this.stateLoader = stateLoader;
    }
    
    public void setStates(Collection<State<S, E>> states) {
        if (states == null || states.isEmpty()) {
            return;
        }
        this.states = new ConcurrentHashMap<E, State<S, E>>();
        for (State<S, E> state : states) {
            this.states.put(state.getEvent(), state);
        }
    }
    
    public void setId(String id) {
        this.id = id;
    }
}
