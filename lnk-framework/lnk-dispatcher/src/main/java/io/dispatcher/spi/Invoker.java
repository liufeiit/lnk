package io.dispatcher.spi;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月2日 下午1:29:58
 */
public interface Invoker<IR, OR, I, O, E extends Throwable> {
    I initialize(IR request) throws E;

    I onStart(I internalRequest) throws E;

    O invoke(I internalRequest) throws E;

    void onComplete(O internalResponse) throws E;

    OR onCompleteReturn(O internalResponse) throws E;

    O onFailure(IR request, Throwable t) throws E;

    OR onFailureReturn(O internalResponse, IR request, Throwable t) throws E;

    void onFinally(IR request, OR response, I internalRequest, O internalResponse) throws E;
}
