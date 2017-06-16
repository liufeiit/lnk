package io.lnk.quartz.srv;

import static org.quartz.JobBuilder.newJob;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;

import io.lnk.api.RemoteObject;
import io.lnk.quartz.api.QuartzJobDetail;
import io.lnk.quartz.api.QuartzJobKey;
import io.lnk.quartz.api.QuartzRemoteJob;
import io.lnk.quartz.api.QuartzService;

/**
 * 定时服务实现类。
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午10:01:08
 */
public class DefaultQuartzService implements QuartzService {
    private Log logger = LogFactory.getLog(this.getClass());
    @Autowired private QuartzTemplate template;

    public void scheduleJob(QuartzJobDetail jobDetail, Trigger trigger, QuartzRemoteJob job) {
        scheduleJob(jobDetail, new Trigger[] {trigger}, job);
    }

    protected void scheduleJobImpl(QuartzJobDetail jobDetail, Trigger[] triggers, QuartzRemoteJob job, boolean adjustTime) {
        QuartzJobKey jobKey = jobDetail.getKey();
        JobDataMap jobDataMap = new JobDataMap();
        for (Entry<String, String> entry : jobDetail.getProperties().entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            jobDataMap.put(name, value);
        }
        String stub = ((RemoteObject) job).serializeStub();
        jobDataMap.put(ReservedJobDataMapKeys.REMOTE_JOB_STUB, stub);
        JobDetail qJobDetail = newJob().withIdentity(jobKey.getName(), jobKey.getGroup()).usingJobData(jobDataMap).build();
        Set<Trigger> triggerList = new HashSet<Trigger>();
        for (Trigger trigger : triggers) {
            if (adjustTime) {
                // 根据Quartz服务器时间调整Trigger
                AdjustTimeGapUtil.adjustTrigger(trigger, 0);
                trigger = trigger.getTriggerBuilder().forJob(qJobDetail).build();
            }
            triggerList.add(trigger);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("schdule job, name=[" + jobKey.getName() + "], group=[" + jobKey.getGroup() + "].");
        }
        template.scheduleJob(qJobDetail, triggerList);
    }

    public void scheduleJob(QuartzJobDetail jobDetail, Trigger[] triggers, QuartzRemoteJob job) {
        scheduleJobImpl(jobDetail, triggers, job, true);
    }

    public boolean cancelJob(QuartzJobKey jobKey) {
        JobKey key = JobKey.jobKey(jobKey.getName(), jobKey.getGroup());
        return template.unscheduleJob(key);
    }

    public boolean existJob(QuartzJobKey jobKey) {
        JobKey key = JobKey.jobKey(jobKey.getName(), jobKey.getGroup());
        return template.existJob(key);
    }
}
