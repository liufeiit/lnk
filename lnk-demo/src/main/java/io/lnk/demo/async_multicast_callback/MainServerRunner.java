package io.lnk.demo.async_multicast_callback;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.lnk.api.RemoteObject;
import io.lnk.api.annotation.Lnkwired;
import io.lnk.api.exception.AppBizException;
import io.lnk.api.utils.CorrelationIds;
import io.lnk.demo.AuthRequest;
import io.lnk.demo.BasicMainServerRunner;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 * 
 * @version 1.0.0
 * @since 2016年2月24日 上午10:48:17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:async/lnk-async-config.xml"})
@DirtiesContext
public class MainServerRunner extends BasicMainServerRunner {

    @Lnkwired
    AuthService defaultAuthService;
    
    @Lnkwired
    AuthCallbackService authCallbackService;

    /**
     * 异步 异步回调 异步组播演示
     */
    @Test
    public void testDefaultAuthService() throws AppBizException {
        System.err.println("defaultAuthService serializeStub : " + ((RemoteObject) defaultAuthService).serializeStub());
        System.err.println("authCallbackService serializeStub : " + ((RemoteObject) authCallbackService).serializeStub());
        AuthRequest request = buildAuthRequest();
        defaultAuthService.auth(request, authCallbackService);
        try {// 为了看到回调结果
            AuthCallbackService.sync.await();
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
        defaultAuthService.auth_multi_cast(request, authCallbackService);
        try {// 为了看到多播回调结果
            AuthCallbackService.sync2.await();
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }
    
    private AuthRequest buildAuthRequest() {
        AuthRequest request = new AuthRequest();
        request.setTxnId(CorrelationIds.buildGuid());
        request.setCardNo("6222021001138822740");
        request.setIdentityNo("413537199112015138");
        request.setMemberId("123456");
        request.setMobile("13522874567");
        request.setName("刘飞");
        return request;
    }
}
