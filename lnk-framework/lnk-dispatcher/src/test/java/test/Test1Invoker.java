package test;

import org.springframework.util.NumberUtils;

import io.lnk.framework.dispatcher.spi.Invoker;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月1日 下午4:50:57
 */
public class Test1Invoker implements Invoker<String, String, Integer, Long, Throwable> {

    @Override
    public Integer initialize(String request) throws Throwable {
        return NumberUtils.parseNumber(request, Integer.class);
    }

    @Override
    public Integer onStart(Integer internalRequest) throws Throwable {
        return internalRequest;
    }

    @Override
    public Long invoke(Integer internalRequest) throws Throwable {
        return new Long(internalRequest + 1);
    }

    @Override
    public void onComplete(Long response) throws Throwable {
        System.err.println("Test1Invoker.onComplete : " + response);
    }

    @Override
    public String onCompleteReturn(Long internalResponse) throws Throwable {
        return "onCompleteReturn : " + internalResponse;
    }

    @Override
    public Long onFailure(String request, Throwable t) throws Throwable {
        t.printStackTrace(System.err);
        return new Long(NumberUtils.parseNumber(request, Long.class) + 1);
    }

    @Override
    public String onFailureReturn(Long internalResponse, String request, Throwable t) throws Throwable {
        return "onFailureReturn : " + internalResponse;
    }

    @Override
    public void onFinally(String request, String response, Integer internalRequest, Long internalResponse) throws Throwable {}
}
