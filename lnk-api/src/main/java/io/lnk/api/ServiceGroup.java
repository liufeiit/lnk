package io.lnk.api;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 上午10:38:24
 */
public class ServiceGroup {
    private String serviceGroup;
    private int serviceGroupWorkerProcessorThreads = 10;

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public int getServiceGroupWorkerProcessorThreads() {
        return serviceGroupWorkerProcessorThreads;
    }

    public void setServiceGroupWorkerProcessorThreads(int serviceGroupWorkerProcessorThreads) {
        this.serviceGroupWorkerProcessorThreads = serviceGroupWorkerProcessorThreads;
    }

    @Override
    public String toString() {
        return "ServiceGroup [serviceGroup=" + serviceGroup + ", serviceGroupWorkerProcessorThreads=" + serviceGroupWorkerProcessorThreads + "]";
    }
}
