package io.lnk.quartz.api;

import io.lnk.api.InvokeType;
import io.lnk.api.ProtocolVersion;
import io.lnk.api.annotation.LnkMethod;
import io.lnk.api.annotation.LnkService;

/**
 * 远程定时任务接口定义类。
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午9:08:09
 */
@LnkService(group = ServiceGroups.LNK_QUARTZ_SRV, protocol = ProtocolVersion.JAVA_NATIVE_PROTOCOL)
public interface QuartzRemoteJob {
    
    /**
     * 执行任务
     */
    @LnkMethod(type = InvokeType.SYNC, timeoutMillis = 60000L)
    void execute(QuartzJobExeContext context);
}
