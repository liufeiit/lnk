package test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.statemachine.spi.StateMachine;
import test.spi.Events;
import test.spi.States;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月17日 下午3:51:01
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-config/statemachine-config.xml"})
@DirtiesContext
public class StateMachineTest {

    @Autowired
    StateMachine<States, Events> stateMachine;

    @Test
    public void testStateMachine() {
        try {
            stateMachine.sendEvent(Events.SUSPEND);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
    }

    @Before
    public void before() throws Exception {

    }

    @After
    public void end() throws Exception {
        System.exit(0);
    }
}
