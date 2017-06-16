package io.lnk.quartz.srv;

import org.quartz.JobExecutionContext;
import org.quartz.listeners.JobListenerSupport;

/**
 * Quartz触发监听器类。
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午9:58:52
 */
public class RemoteJobListener extends JobListenerSupport {
    /**
     * 远程任务调用器
     */
    private RemoteJobCaller remoteJobCaller;

    public RemoteJobListener(RemoteJobCaller caller) {
        this.remoteJobCaller = caller;
    }

    public String getName() {
        return "remoteJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        remoteJobCaller.call(context);
    }
}
