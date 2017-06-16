package io.lnk.quartz.srv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Quartz服务器主程序类。
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午9:57:29
 */
public class QuartzServerMain {

    private static Log logger = LogFactory.getLog(QuartzServerMain.class);

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"spring-config/lnk-quartz-srv.xml"});
        context.registerShutdownHook();
        logger.info("Lnk-Quartz Server started.");
        System.out.println("Lnk-Quartz Server started.");
    }
}
