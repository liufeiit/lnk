package io.lnk.demo.sync_multi_version_ploy;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StreamUtils;

import io.lnk.api.RemoteObject;
import io.lnk.api.annotation.Lnkwired;
import io.lnk.api.protocol.Serializer;
import io.lnk.api.utils.CorrelationIds;
import io.lnk.demo.AppBizException;
import io.lnk.demo.AuthRequest;
import io.lnk.demo.AuthResponse;
import io.lnk.demo.BasicMainServerRunner;
import io.lnk.protocol.jackson.JacksonSerializer;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 * 
 * @version 1.0.0
 * @since 2016年2月24日 上午10:48:17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:sync/lnk-sync-config.xml"})
@DirtiesContext
public class MainServerRunner extends BasicMainServerRunner {

    @Lnkwired
    AuthService defaultAuthService;

    @Lnkwired(version = "2.0.0")
    AuthService v2AuthService;

    static Serializer serializer = new JacksonSerializer();

    /**
     * 多态演示
     */
    @Test
    public void testDefaultAuthServicePoly() throws Exception {
        try {
            System.err.println("defaultAuthService serializeStub : " + ((RemoteObject) defaultAuthService).serializeStub());
            PolyAuthRequest request = buildPolyAuthRequest();
            FileInputStream in = new FileInputStream("/Users/liufei/软件/Office2010/Office_2010激活工具.exe");
            request.setData(StreamUtils.copyToByteArray(in));
            PolyAuthResponse response = (PolyAuthResponse) defaultAuthService.auth_poly(request);
            FileOutputStream out = new FileOutputStream("/Users/liufei/软件/Office2010/Office_2010激活工具.copy3.exe", false);
            StreamUtils.copy(new ByteArrayInputStream(response.getData()), out);
            out.close();
            // System.err.println("response : " + JSON.toJSONString(response, true));
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * 默认版本服务演示
     */
    @Test
    public void testDefaultAuthService() throws AppBizException {
        System.err.println("defaultAuthService serializeStub : " + ((RemoteObject) defaultAuthService).serializeStub());
        AuthRequest request = buildAuthRequest();
        AuthResponse response = defaultAuthService.auth(request);
        System.err.println("response : " + response);
    }

    /**
     * 多版本服务演示
     */
    @Test
    public void testV2AuthService() throws AppBizException {
        System.err.println("v2AuthService serializeStub : " + ((RemoteObject) v2AuthService).serializeStub());
        AuthRequest request = buildAuthRequest();
        AuthResponse response = v2AuthService.auth(request);
        System.err.println("response : " + response);
    }

    /**
     * 异常演示
     */
    @Test
    public void testDefaultAuthServiceException() {
        try {
            System.err.println("defaultAuthService serializeStub : " + ((RemoteObject) defaultAuthService).serializeStub());
            AuthRequest request = buildAuthRequest();
            request.setName("异常");
            AuthResponse response = defaultAuthService.auth(request);
            System.err.println("response : " + response);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static PolyAuthRequest buildPolyAuthRequest() {
        PolyAuthRequest request = new PolyAuthRequest();
        request.setTxnId(CorrelationIds.buildGuid());
        request.setCardNo("6222021001138822740");
        request.setIdentityNo("413537199112015138");
        request.setMemberId("123456");
        request.setMobile("13522874567");
        request.setName("刘飞");

        request.setPrincipalId("101");
        request.setTxnType("00001");
        request.setProductType("QP");
        request.setIdType("01");
        return request;
    }

    private static AuthRequest buildAuthRequest() {
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
