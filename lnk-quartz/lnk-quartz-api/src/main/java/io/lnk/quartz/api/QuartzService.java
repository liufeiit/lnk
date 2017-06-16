package io.lnk.quartz.api;

import org.quartz.Trigger;

import io.lnk.api.ProtocolVersion;
import io.lnk.api.annotation.LnkService;

/**
 * 定时服务接口定义类。
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午9:12:19
 */
@LnkService(group = ServiceGroups.LNK_QUARTZ_SRV, protocol = ProtocolVersion.JAVA_NATIVE_PROTOCOL)
public interface QuartzService {
    /**
     * 计划定时任务
     */
    void scheduleJob(QuartzJobDetail jobDetail, Trigger trigger, QuartzRemoteJob job);

    /**
     * 计划定时任务（多次触发）
     */
    void scheduleJob(QuartzJobDetail jobDetail, Trigger[] triggers, QuartzRemoteJob job);

    /**
     * 撤销定时任务
     */
    boolean cancelJob(QuartzJobKey jobKey);

    /**
     * 是否存在定时任务
     */
    boolean existJob(QuartzJobKey jobKey);
}
