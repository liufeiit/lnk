package io.statemachine.spi.diagram;

import java.util.Collection;

import io.statemachine.spi.ResourcesStateLoader;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午3:15:30
 */
public interface Diagram<S, E> {
    
    String getId();

    Collection<State<S, E>> getStates();
    
    State<S, E> getState(E event);
    
    ResourcesStateLoader<S, E> getStateLoader();
}
