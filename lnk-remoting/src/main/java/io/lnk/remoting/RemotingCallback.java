package io.lnk.remoting;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月21日 下午10:29:54
 */
public interface RemotingCallback {
    void onComplete(final ReplyFuture replyFuture);
}
