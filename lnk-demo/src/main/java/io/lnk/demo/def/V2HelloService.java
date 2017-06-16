package io.lnk.demo.def;

import io.lnk.api.annotation.LnkServiceVersion;
import io.lnk.demo.BasicService;
import io.lnk.demo.HelloService;
import io.lnk.demo.WelcomeCallback;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午2:58:27
 */
@LnkServiceVersion(version = "2.0.0")
public class V2HelloService extends BasicService implements HelloService {

    @Override
    public ComplexResponse welcome(String name, ComplexRequest request) {
//        throw new RuntimeException("test Error.");
        ComplexResponse response = new ComplexResponse();
        response.setName("V2你好 " + name);
        response.setExt(("V2你好 " + name + ", " + request.getName() + " " + request.getAge() + " " + new String(request.getExt())).getBytes());
        return response;
    }

    @Override
    public void welcome(String name, WelcomeCallback callback) {
        System.err.println("V2hello " + name );
        callback.callback("V2我是回调过来的 hello " + name );
    }

    @Override
    public void welcomeMulticast(String name) {
        System.err.println("V2Multicast : hello " + name + ", " + demoService.demo(name));
    }

    @Override
    public void welcomeMulticast(String name, WelcomeCallback callback) {
        System.err.println("V2Multicast : hello " + name + ", " + demoService.demo(name));
        callback.callback("V2Multicast : 我是回调过来的 hello " + name + ", " + demoService.demo(name));
    }

    @Override
    public void welcome(String name) {
       // System.err.println("hello " + name + ", " + demoService.demo(name));
    	demoService.demo("V2" + name);
    }
}
