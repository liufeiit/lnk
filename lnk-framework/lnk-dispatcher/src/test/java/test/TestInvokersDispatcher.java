package test;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月2日 下午3:38:07
 */
public class TestInvokersDispatcher {

    public static void main(String[] args) throws Throwable {
        String configLocation = "dispatcher-config.xml";
        AbstractApplicationContext context = new ClassPathXmlApplicationContext(configLocation);
        context.registerShutdownHook();
        System.out.println("Invoker dispatcher started.");
        TestInvokerAgent testInvokerAgent = context.getBean("TestInvokersDispatcher", TestInvokerAgent.class);
        System.err.println("testInvokerAgent : " + testInvokerAgent.invoke(TestInvokerTypeCode.Test_001, "123456"));
        System.err.println("testInvokerAgent : " + testInvokerAgent.invoke(TestInvokerTypeCode.Test_002, "123456"));
    }
}
