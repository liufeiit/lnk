package io.lnk.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月23日 下午3:56:44
 */
public class LnkNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("lnk", new LnkEndpointParser());
    }
}
