package io.lnk.demo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.Future;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StreamUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;

import io.lnk.api.BrokerProtocols;
import io.lnk.api.Protocols;
import io.lnk.api.broker.BrokerArg;
import io.lnk.api.broker.BrokerCaller;
import io.lnk.api.broker.BrokerCommand;
import io.lnk.api.protocol.Serializer;
import io.lnk.api.utils.CorrelationIds;
import io.lnk.demo.sync_multi_version_ploy.AuthService;
import io.lnk.protocol.jackson.JacksonSerializer;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午8:41:42
 */
public class BrokerServerMain {
    private static final Logger log = LoggerFactory.getLogger(BrokerServerMain.class);
    static Serializer serializer = new JacksonSerializer();
    static BrokerCaller brokerCaller;

    public static void main(String[] args) {
        String configLocation = "sync/lnk-sync-config.xml";
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(configLocation);
        ctx.registerShutdownHook();
        log.info("BrokerServer start.");
        brokerCaller = ctx.getBean(BrokerCaller.class);
        brokerInvokeHttp();
        brokerInvokeWs();
    }

    public static void brokerInvokeHttp() {
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
            System.err.println("http client send BrokerCommand : " + command);
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

    public static void brokerInvokeWs() {
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
            for (int i = 0; i < 1; i++)
                session.getRemote().sendString(command, new WriteCallback() {
                    public void writeSuccess() {
                        System.err.println("send success.");
                    }
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

    private static AuthRequest buildAuthRequest() {
        AuthRequest request = new AuthRequest();
        request.setTxnId(CorrelationIds.buildGuid());
        request.setCardNo("6222021001138822740");
        request.setIdentityNo("413537199112015138");
        request.setMemberId("123456");
        request.setMobile("13522874567");
        request.setName("刘飞");
        try {
            String file = "/Users/liufei/软件/Office2010/Office_2010激活工具.exe";
            String data = Base64.encodeBase64String(StreamUtils.copyToByteArray(new FileInputStream(file)));
            request.setDataString(StringUtils.substring(data, 0, 10000));
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        return request;
    }
}
