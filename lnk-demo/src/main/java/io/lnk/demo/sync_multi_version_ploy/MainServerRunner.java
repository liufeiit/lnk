package io.lnk.demo.sync_multi_version_ploy;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StreamUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;

import io.lnk.api.BrokerProtocols;
import io.lnk.api.Protocols;
import io.lnk.api.RemoteObject;
import io.lnk.api.annotation.Lnkwired;
import io.lnk.api.broker.BrokerArg;
import io.lnk.api.broker.BrokerCaller;
import io.lnk.api.broker.BrokerCommand;
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

    @Autowired
    BrokerCaller brokerCaller;

    static Serializer serializer = new JacksonSerializer();

    public static void main23(String[] args) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            String uri = "http://10.10.19.174:42000";
            HttpPost request = new HttpPost(uri);
            BrokerCommand brokerCommand = new BrokerCommand();
            brokerCommand.setInvokeType(BrokerCommand.SYNC);
            brokerCommand.setApplication("test.broker");
            brokerCommand.setVersion("2.0.0");
            brokerCommand.setProtocol(Protocols.DEFAULT_PROTOCOL);
            brokerCommand.setBrokerProtocol(BrokerProtocols.JACKSON);
            brokerCommand.setServiceGroup("biz-pay-bgw-payment.srv");
            brokerCommand.setServiceId(AuthService.class.getName());
            brokerCommand.setMethod("auth");
            brokerCommand.setSignature(new String[] {AuthRequest.class.getName()});
            BrokerArg arg = new BrokerArg();
            arg.setType(AuthRequest.class.getName());
            arg.setArg(serializer.serializeAsString(buildAuthRequest()));
            brokerCommand.setArgs(new BrokerArg[] {arg});
            brokerCommand.setTimeoutMillis(Long.MAX_VALUE);
            String command = serializer.serializeAsString(brokerCommand);
            HttpEntity entity = new StringEntity(command);
            request.setEntity(entity);
            BrokerCommand response = client.execute(HttpHost.create(uri), request, new AbstractResponseHandler<BrokerCommand>() {
                public BrokerCommand handleEntity(HttpEntity entity) throws IOException {
                    return serializer.deserialize(BrokerCommand.class, StreamUtils.copyToString(entity.getContent(), Charsets.UTF_8));
                }
            });
            System.err.println("http client Received BrokerCommand : " + JSON.toJSONString(response, true));
        } catch (ClientProtocolException e) {
            e.printStackTrace(System.err);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public static class EventSocket extends WebSocketAdapter {
        public void onWebSocketConnect(Session sess) {
            super.onWebSocketConnect(sess);
            System.out.println("Socket Connected: " + sess);
        }

        public void onWebSocketText(String message) {
            super.onWebSocketText(message);
            System.out.println("Received TEXT message: " + message);
            BrokerCommand response = serializer.deserialize(BrokerCommand.class, message);
            System.err.println("Received BrokerCommand : " + JSON.toJSONString(response, true));
        }

        public void onWebSocketClose(int statusCode, String reason) {
            super.onWebSocketClose(statusCode, reason);
            System.out.println("Socket Closed: [" + statusCode + "] " + reason);
        }

        public void onWebSocketError(Throwable cause) {
            super.onWebSocketError(cause);
            cause.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {
        URI uri = URI.create("ws://10.10.19.174:43000");
        WebSocketClient client = new WebSocketClient();
        try {
            client.start();
            EventSocket socket = new EventSocket();
            Future<Session> fut = client.connect(socket, uri);
            Session session = fut.get();
            BrokerCommand brokerCommand = new BrokerCommand();
            brokerCommand.setInvokeType(BrokerCommand.SYNC);
            brokerCommand.setApplication("test.broker");
            brokerCommand.setVersion("2.0.0");
            brokerCommand.setProtocol(Protocols.DEFAULT_PROTOCOL);
            brokerCommand.setBrokerProtocol(BrokerProtocols.JACKSON);
            brokerCommand.setServiceGroup("biz-pay-bgw-payment.srv");
            brokerCommand.setServiceId(AuthService.class.getName());
            brokerCommand.setMethod("auth");
            brokerCommand.setSignature(new String[] {AuthRequest.class.getName()});
            BrokerArg arg = new BrokerArg();
            arg.setType(AuthRequest.class.getName());
            arg.setArg(serializer.serializeAsString(buildAuthRequest()));
            brokerCommand.setArgs(new BrokerArg[] {arg});
            brokerCommand.setTimeoutMillis(Long.MAX_VALUE);
            String command = serializer.serializeAsString(brokerCommand);
            for (int i = 0; i < 100; i++)
                session.getRemote().sendString(command, new WriteCallback() {

                    @Override
                    public void writeSuccess() {
                        System.err.println("send success.");
                    }

                    @Override
                    public void writeFailed(Throwable x) {
                        x.printStackTrace();
                    }
                });
            // session.close();
            System.err.println("send ws command : " + command);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            // try {
            // client.stop();
            // } catch (Throwable e) {
            // e.printStackTrace(System.err);
            // }
        }
    }

    @Test
    public void testBrokerCaller() {
        try {
            BrokerCommand brokerCommand = new BrokerCommand();
            brokerCommand.setInvokeType(BrokerCommand.SYNC);
            brokerCommand.setApplication("test.broker");
            brokerCommand.setVersion("2.0.0");
            brokerCommand.setProtocol(Protocols.DEFAULT_PROTOCOL);
            brokerCommand.setBrokerProtocol(BrokerProtocols.JACKSON);
            brokerCommand.setServiceGroup("biz-pay-bgw-payment.srv");
            brokerCommand.setServiceId(AuthService.class.getName());
            brokerCommand.setMethod("auth");
            brokerCommand.setSignature(new String[] {AuthRequest.class.getName()});
            BrokerArg arg = new BrokerArg();
            arg.setType(AuthRequest.class.getName());
            arg.setArg(serializer.serializeAsString(buildAuthRequest()));
            brokerCommand.setArgs(new BrokerArg[] {arg});
            brokerCommand.setTimeoutMillis(Long.MAX_VALUE);
            String command = serializer.serializeAsString(brokerCommand);
            String response = brokerCaller.invoke(command);
            // System.err.println("request command : " + JSON.toJSONString(brokerCommand, true));
            // System.err.println("response command : " + JSON.toJSONString(response, true));
            System.err.println("request command : " + command);
            System.err.println("response command : " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        System.err.println("response : " + JSON.toJSONString(response, true));
    }

    /**
     * 多版本服务演示
     */
    @Test
    public void testV2AuthService() throws AppBizException {
        System.err.println("v2AuthService serializeStub : " + ((RemoteObject) v2AuthService).serializeStub());
        AuthRequest request = buildAuthRequest();
        AuthResponse response = v2AuthService.auth(request);
        System.err.println("response : " + JSON.toJSONString(response, true));
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
            System.err.println("response : " + JSON.toJSONString(response, true));
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
