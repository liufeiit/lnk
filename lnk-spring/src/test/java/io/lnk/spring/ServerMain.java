package io.lnk.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午8:41:42
 */
public class ServerMain {
    
    private static final Logger log = LoggerFactory.getLogger(ServerMain.class);

    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        String configLocation = "lnk-config.xml";
        AbstractApplicationContext context = new ClassPathXmlApplicationContext(configLocation);
        context.registerShutdownHook();
        log.info("LNK Server started.");
        System.out.println("LNK Server started.");
    }
}
