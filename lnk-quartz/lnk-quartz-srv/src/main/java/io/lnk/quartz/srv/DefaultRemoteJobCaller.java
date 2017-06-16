package io.lnk.quartz.srv;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;

import io.lnk.api.RemoteObjectFactory;
import io.lnk.api.RemoteObjectFactoryAware;
import io.lnk.quartz.api.QuartzJobExeContext;
import io.lnk.quartz.api.QuartzRemoteJob;

/**
 * 缺省远程任务调用器类。
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午9:52:47
 */
public class DefaultRemoteJobCaller implements RemoteJobCaller, RemoteObjectFactoryAware {
    protected Log logger = LogFactory.getLog(getClass());
    private RemoteObjectFactory remoteObjectFactory;

    private void callImpl(final JobExecutionContext ctx) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fired the remoteObject.");
        }
        final String stub = ctx.getMergedJobDataMap().getString(ReservedJobDataMapKeys.REMOTE_JOB_STUB);
        QuartzRemoteJob remoteJob = remoteObjectFactory.getRemoteStub(QuartzRemoteJob.class, stub);
        final QuartzJobExeContext exeCtx = new QuartzJobExeContext();
        exeCtx.setFireTime(ctx.getFireTime());
        exeCtx.setNextFireTime(ctx.getNextFireTime());
        exeCtx.setPrevFireTime(ctx.getPreviousFireTime());
        exeCtx.setRecovering(ctx.isRecovering());
        exeCtx.setRefireCount(ctx.getRefireCount());
        exeCtx.setScheduledFireTime(ctx.getScheduledFireTime());
        exeCtx.setJobName(ctx.getJobDetail().getKey().getName());
        exeCtx.setJobGroup(ctx.getJobDetail().getKey().getGroup());
        Map<String, String> props = new HashMap<String, String>();
        for (String key : ctx.getMergedJobDataMap().getKeys()) {
            props.put(key, ctx.getMergedJobDataMap().getString(key));
        }
        exeCtx.setProperties(props);
        remoteJob.execute(exeCtx);
    }

    public void call(JobExecutionContext ctx) {
        try {
            callImpl(ctx);
        } catch (Throwable e) {
            logger.error("call Remote Job meet Error.", e);
        }
    }

    public void setRemoteObjectFactory(RemoteObjectFactory factory) {
        this.remoteObjectFactory = factory;
    }
}
