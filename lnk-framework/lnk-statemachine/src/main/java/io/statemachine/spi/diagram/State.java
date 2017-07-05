package io.statemachine.spi.diagram;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月19日 下午3:16:10
 */
public interface State<S, E> {
    
    E getEvent();
    
    S getSource();
    
    S getTarget();
    
    Task<S, E> getTask();

}
