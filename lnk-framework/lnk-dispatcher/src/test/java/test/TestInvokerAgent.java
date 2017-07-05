package test;

import io.lnk.framework.dispatcher.spi.InvokerCode;
import io.lnk.framework.dispatcher.spi.InvokerMethod;
import io.lnk.framework.dispatcher.spi.InvokerRequest;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月2日 下午3:40:12
 */
public interface TestInvokerAgent {

    @InvokerMethod
    String invoke(@InvokerCode TestInvokerTypeCode code, @InvokerRequest String request);
}
