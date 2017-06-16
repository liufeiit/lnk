package io.lnk.quartz.srv;

import java.util.Set;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;

/**
 * 定时服务
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午9:51:57
 */
public interface QuartzTemplate {
    /**
     * 计划任务
     */
    void scheduleJob(JobDetail jobDetail, Set<Trigger> triggers);

    /**
     * 撤销任务
     */
    boolean unscheduleJob(JobKey key);

    /**
     * 任务是否存在
     */
    boolean existJob(JobKey key);
}
