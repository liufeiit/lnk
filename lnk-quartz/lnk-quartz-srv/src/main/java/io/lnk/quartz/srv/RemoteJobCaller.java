package io.lnk.quartz.srv;

import org.quartz.JobExecutionContext;

/**
 * 远程Job呼叫器接口定义类。
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午9:58:26
 */
public interface RemoteJobCaller {
	void call(JobExecutionContext ctx);
}
