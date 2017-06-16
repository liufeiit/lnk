package io.lnk.demo.def;

import io.lnk.demo.BasicService;
import io.lnk.demo.DemoService;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午8:53:29
 */
//@LnkServiceVersion(version = "2.0.0")
public class DefaultDemoService extends BasicService implements DemoService {

    @Override
    public String demo(String name) {
        return "I'm " + name;
    }
}
