package test;

import io.lnk.framework.dispatcher.spi.InvokerTypeCode;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月2日 下午3:32:37
 */
public enum TestInvokerTypeCode implements InvokerTypeCode {

    Test_001("V1000"),

    Test_002("V2000"),
    
    ;
    
    private final String invokerCode;
    
    private TestInvokerTypeCode(String invokerCode) {
        this.invokerCode = invokerCode;
    }

    @Override
    public String invokerCode() {
        return invokerCode;
    }
}
