package io.lnk.demo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import io.lnk.api.RemoteObjectFactory;
import io.lnk.api.ServiceVersion;
import io.lnk.demo.HelloService.ComplexRequest;
import io.lnk.demo.HelloService.ComplexResponse;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午8:41:42
 */
public class ServerMain {
    private static final Logger log = LoggerFactory.getLogger(ServerMain.class);
    static ClassPathXmlApplicationContext context;
    static HelloService helloService;
    static RemoteObjectFactory factory;
    static WelcomeCallback callback;
    static {
        System.setProperty("host.name", "lf");
        System.setProperty("app.name", "lnk.test");
        System.setProperty("ins.num", "1");
        System.setProperty("lf.lnk.test.1.port", "30000");
        String configLocation = "lnk-config.xml";
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(configLocation);
        ctx.registerShutdownHook();
        ServerMain.context = ctx;
        ServerMain.factory = ctx.getBean(RemoteObjectFactory.class);
        ServerMain.helloService = ServerMain.factory.getServiceObject(HelloService.class, "2.0.0");
        ServerMain.callback = ServerMain.factory.getServiceObject(WelcomeCallback.class, ServiceVersion.DEFAULT_VERSION);
        log.info("LNK Server started.");
        System.out.println("LNK Server started.");
    }

    public static void main1(String[] args) {
        ComplexRequest request = new ComplexRequest();
        request.setName("哈哈");
        request.setAge(60);
        request.setExt("信不信".getBytes());
        try {
            // Thread.sleep(20000L);
            ComplexResponse response = helloService.welcome("刘飞", request);
            System.err.println(response.getName() + " ext : " + new String(response.getExt()));
            helloService.welcomeMulticast("哈哈");
            helloService.welcome("测试回调", callback);
            System.err.println("调用结束");
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
        // 测试自动注册中心恢复
        // while (true) {
        // try {
        // ComplexResponse response = helloService.welcome("刘飞", request);
        // System.err.println(response.getName() + " ext : " + new String(response.getExt()));
        // Thread.sleep(1000L);
        // } catch (Throwable e) {
        // e.printStackTrace(System.err);
        // }
        // }
    }

    public static void main(String[] args) throws Exception {
        ExecutorService exec = Executors.newCachedThreadPool();
        final Set<Integer> fail = new HashSet<Integer>();
        final Set<Integer> succ = new HashSet<Integer>();
        int concurrentNum = 50;
        int clientNum = 1000;
        final CountDownLatch countDownLatch = new CountDownLatch(clientNum);
        final Semaphore semp = new Semaphore(concurrentNum);
        for (int i = 0; i < clientNum; i++) {
            final int num = i;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        semp.acquire();
                        ComplexRequest request = new ComplexRequest();
                        request.setName("哈哈");
                        request.setAge(60);
                        request.setExt("信不信".getBytes());
                        final ComplexResponse response = helloService.welcome("刘飞", request);
                        System.err.println(response.getName() + " ext : " + new String(response.getExt()));
                        succ.add(num);
                        semp.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.err);
                        fail.add(num);
                        semp.release();
                    }
                    countDownLatch.countDown();
                }
            };
            exec.execute(run);
        }
        countDownLatch.await();
        exec.shutdown();
        exec = null;
        System.err.println("成功请求 " + succ.size());
        System.err.println("失败请求 " + fail.size() + " fail : " + fail);
    }
}
