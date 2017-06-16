package io.lnk.flow;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.api.flow.FlowController;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月25日 下午2:18:15
 */
public class SemaphoreFlowController implements FlowController {
    private static final Logger log = LoggerFactory.getLogger(SemaphoreFlowController.class.getSimpleName());
    private final Semaphore semaphore;
    
    public SemaphoreFlowController(int permits) {
        super();
        this.semaphore = new Semaphore(permits, true);;
    }

    @Override
    public boolean tryAcquireFailure(long timeoutMillis) {
        try {
            return !semaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            log.error("Server tryAcquire Error.", e);
        }
        return true;
    }

    @Override
    public void release() {
        semaphore.release();
    }
}
