package io.lnk.demo.def;

import io.lnk.demo.WelcomeCallback;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月27日 下午9:36:51
 */
//@LnkServiceVersion(version = "2.0.0")
public class DefaultWelcomeCallback implements WelcomeCallback {

    @Override
    public void callback(String arg) {
        System.err.println("我是回调的结果 : " + arg);
    }
}
