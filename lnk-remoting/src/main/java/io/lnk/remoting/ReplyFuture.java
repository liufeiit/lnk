package io.lnk.remoting;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.lnk.remoting.protocol.RemotingCommand;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月19日 下午6:52:06
 */
public class ReplyFuture {
    private final long opaque;
    private final long timeoutMillis;
    private final long startInMillis = System.currentTimeMillis();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private RemotingCallback callback;
    
    private volatile RemotingCommand response;
    private volatile boolean sent = true;
    private volatile Throwable cause;
    
    public ReplyFuture(long opaque, long timeoutMillis) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() - this.startInMillis >= this.timeoutMillis;
    }
    
    public boolean isTimeout(long timeoutMillis) {
        return System.currentTimeMillis() - this.startInMillis >= timeoutMillis;
    }

    public RemotingCommand waitFor(final long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.response;
    }
    
    public boolean isAsyncCallback() {
        return callback != null;
    }
    
    public void invokeCallback() {
        if (callback == null) {
            return;
        }
        callback.onComplete(this);
    }

    public void setReply(final RemotingCommand response) {
        this.response = response;
        this.countDownLatch.countDown();
    }
    
    public void setResponse(RemotingCommand response) {
        this.response = response;
    }
    
    public RemotingCommand getResponse() {
        return response;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public long getOpaque() {
        return opaque;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
    
    public void setCallback(RemotingCallback callback) {
        this.callback = callback;
    }
    
    public long getStartInMillis() {
        return startInMillis;
    }
    
    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    @Override
    public String toString() {
        return "ReplyFuture [opaque=" + opaque + ", response=" + response + ", sent=" + sent + "]";
    }
}
