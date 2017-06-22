package io.lnk.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午8:41:42
 */
public class BrokerServerMain {
    private static final Logger log = LoggerFactory.getLogger(BrokerServerMain.class);

    public static void main(String[] args) {
        String configLocation = "sync/lnk-sync-config.xml";
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(configLocation);
        ctx.registerShutdownHook();
        log.info("BrokerServer start.");
    }
}
