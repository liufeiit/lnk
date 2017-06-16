package io.lnk.quartz.srv;

import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 缺省Quartz模版类。
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月13日 下午9:44:37
 */
public class DefaultQuartzTemplate implements QuartzTemplate, InitializingBean, DisposableBean {
    private Log logger = LogFactory.getLog(getClass());
    private static final String DEFAULT_DISCARD_IDLE_CONNECTIONS_SECONDS = "3600";
    private Properties properties = new Properties();
    private SchedulerFactory schedulerFactory;
    private Scheduler scheduler;
    @Autowired private RemoteJobCaller remoteJobCaller;

    public DefaultQuartzTemplate() {
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        properties.setProperty("org.quartz.jobStore.dataSource", "quartzDs");
        properties.setProperty("org.quartz.threadPool.threadCount", "3");
        properties.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
    }

    public void afterPropertiesSet() throws Exception {
        this.setDiscardIdleConnectionsSeconds(DEFAULT_DISCARD_IDLE_CONNECTIONS_SECONDS);
        schedulerFactory = new StdSchedulerFactory(properties);
        scheduler = schedulerFactory.getScheduler();
        scheduler.getListenerManager().addJobListener(new RemoteJobListener(remoteJobCaller));
        scheduler.start();
    }

    public void destroy() throws Exception {
        scheduler.shutdown();
    }

    public void scheduleJob(JobDetail jobDetail, Set<Trigger> triggers) {
        try {
            scheduler.scheduleJob(jobDetail, triggers, true);
        } catch (SchedulerException e) {
            logger.error("ScheduleJob meet error.", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean unscheduleJob(JobKey jobKey) {
        for (int i = 0; i < 3; i++) {
            try {
                return scheduler.deleteJob(jobKey);
            } catch (SchedulerException e) {
                if (i == 2) {
                    logger.error("UnscheduleJob meet error, ignored error.", e);
                }
            }
        }
        return false;
    }

    public boolean existJob(JobKey key) {
        try {
            return scheduler.checkExists(key);
        } catch (SchedulerException e) {
            logger.error("CheckExist job meet error.", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void setJdbcDriver(String driver) {
        properties.setProperty("org.quartz.dataSource.quartzDs.driver", driver);
    }

    public void setJdbcUrl(String url) {
        properties.setProperty("org.quartz.dataSource.quartzDs.URL", url);
    }

    public void setJdbcUser(String user) {
        properties.setProperty("org.quartz.dataSource.quartzDs.user", user);
    }

    public void setJdbcPassword(String password) {
        properties.setProperty("org.quartz.dataSource.quartzDs.password", password);
    }

    public void setMaxConnections(String maxConnections) {
        properties.setProperty("org.quartz.dataSource.quartzDs.maxConnections", maxConnections);
    }

    public void setValidationQuery(String validationQuery) {
        properties.setProperty("org.quartz.dataSource.quartzDs.validationQuery", validationQuery);
    }

    public void setDiscardIdleConnectionsSeconds(String discardIdleConnectionsSeconds) {
        properties.setProperty("org.quartz.dataSource.quartzDs.discardIdleConnectionsSeconds", discardIdleConnectionsSeconds);
    }

    public void setJdbcConnectionProviderClass(String jdbcConnectionProviderClass) {
        properties.setProperty("org.quartz.dataSource.quartzDs.connectionProvider.class", jdbcConnectionProviderClass);
    }

    public void setConcurrent(int concurrent) {
        properties.setProperty("org.quartz.threadPool.threadCount", String.valueOf(concurrent));
    }

    public void setJobStoreDriverDelegateClass(String jobStoreDriverDelegateClass) {
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", jobStoreDriverDelegateClass);
    }

    public void setJobStoreUseProperties(String useProps) {
        properties.setProperty("org.quartz.jobStore.useProperties", useProps);
    }
}
