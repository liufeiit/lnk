package test;

import io.lnk.framework.dispatcher.spi.InvokerTypeCode;
import io.lnk.framework.utils.FieldRetriever;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月1日 下午10:59:39
 */
public class FieldRetrieverTest {
    
    public static void main(String[] args) throws Throwable {
        InvokerTypeCode actionCode = FieldRetriever.getObject("test.FieldRetrieverTest.InvokerTypeCodeEnum.A1", InvokerTypeCode.class);
        System.err.println("ActionCode : " + actionCode.invokerCode());
        
    }

    public static enum InvokerTypeCodeEnum implements InvokerTypeCode {

        A1("A1-Code"), A2("A2-Code")
        
        ;
        
        private final String actionCode;
        
        private InvokerTypeCodeEnum(String actionCode) {
            this.actionCode = actionCode;
        }

        @Override
        public String invokerCode() {
            return actionCode;
        }
    }
}
