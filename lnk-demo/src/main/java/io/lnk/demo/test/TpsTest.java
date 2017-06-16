package io.lnk.demo.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import io.lnk.demo.HelloService;
import io.lnk.demo.ServerMain;
import io.lnk.demo.HelloService.ComplexRequest;
import io.lnk.demo.HelloService.ComplexResponse;
import io.lnk.demo.test.thread.ConcurrentThreadMetrics;
import io.lnk.demo.test.thread.ResponseType;
import io.lnk.demo.test.thread.RunWork;

public class TpsTest {
	private static final Logger log = LoggerFactory.getLogger(ServerMain.class);
	static ClassPathXmlApplicationContext context;
	static HelloService helloService;
	static {
		System.setProperty("host.name", "lf");
		System.setProperty("app.name", "lnk.test");
		System.setProperty("ins.num", "1");
		System.setProperty("lf.lnk.test.1.port", "30000");
		String configLocation = "lnk-config.xml";
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(configLocation);
		ctx.registerShutdownHook();
		final HelloService helloService = ctx.getBean(HelloService.class);
		TpsTest.context = ctx;
		TpsTest.helloService = helloService;
		log.info("LNK Server started.");
		System.out.println("LNK Server started.");
	}

	public static ResponseType testTps() {
		try {
//			long startTime = System.currentTimeMillis();
//		    ComplexRequest request = new ComplexRequest();
//	        request.setName("哈哈");
//	        request.setAge(60);
//	        request.setExt("信不信".getBytes());
//	        final ComplexResponse response = helloService.welcome("刘飞", request);
//	        System.err.println(response.getName() + " ext : " + new String(response.getExt()));
			helloService.welcome("test");
//			System.out.println(System.currentTimeMillis() - startTime);
			return ResponseType.Success;
		} catch (Exception e) {
			return ResponseType.Fail;
		}
	}

	public static void testConcurrent(long ctTime, long maxCount, int maxThreads) {
		RunWork work = new RunWork() {
			@Override
			public ResponseType run() {
				return testTps();
			}
		};
		ConcurrentThreadMetrics.start(work, ctTime, maxCount, maxThreads);
		ConcurrentThreadMetrics.dumpResult(ctTime, maxCount, maxThreads);
	}

	public static void main(String[] args) {
		if (args == null || args.length != 3)
			args = new String[] { "5000000", "0", "9" };
		// testTps();
		testConcurrent(Long.valueOf(args[0]).longValue(), Long.valueOf(args[1]), Long.valueOf(args[2]).intValue());
	}
}
