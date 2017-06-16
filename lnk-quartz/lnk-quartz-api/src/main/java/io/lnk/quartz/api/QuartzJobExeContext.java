package io.lnk.quartz.api;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 任务执行上下文类。
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午9:11:35
 */
public class QuartzJobExeContext implements Serializable {
    private static final long serialVersionUID = -365126096662154510L;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务组
     */
    private String jobGroup;

    /**
     * 任务触发时间
     */
    private Date fireTime;

    /**
     * 下次触发时间
     */
    private Date nextFireTime;

    /**
     * 上次触发时间
     */
    private Date prevFireTime;

    /**
     * 重触发尝试次数
     */
    private int refireCount;

    /**
     * 计划触发时间
     */
    private Date scheduledFireTime;

    /**
     * 恢复标志（Quartz停机后补触发）
     */
    private boolean recovering;

    /**
     * 上下文参数集
     */
    private Map<String, String> properties;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public Date getFireTime() {
        return fireTime;
    }

    public void setFireTime(Date fireTime) {
        this.fireTime = fireTime;
    }

    public Date getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public Date getPrevFireTime() {
        return prevFireTime;
    }

    public void setPrevFireTime(Date prevFireTime) {
        this.prevFireTime = prevFireTime;
    }

    public int getRefireCount() {
        return refireCount;
    }

    public void setRefireCount(int refireCount) {
        this.refireCount = refireCount;
    }

    public Date getScheduledFireTime() {
        return scheduledFireTime;
    }

    public void setScheduledFireTime(Date scheduledFireTime) {
        this.scheduledFireTime = scheduledFireTime;
    }

    public boolean isRecovering() {
        return recovering;
    }

    public void setRecovering(boolean recovering) {
        this.recovering = recovering;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "QuartzJobExeContext [jobName=" + jobName + ", jobGroup=" + jobGroup + ", fireTime=" + fireTime + ", nextFireTime=" + nextFireTime + ", prevFireTime=" + prevFireTime + ", refireCount="
                + refireCount + ", scheduledFireTime=" + scheduledFireTime + ", recovering=" + recovering + ", properties=" + properties + "]";
    }
}
