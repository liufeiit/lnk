package io.lnk.remoting;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.lnk.api.ClientConfiguration;
import io.lnk.api.ServerConfiguration;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.protocol.LnkProtocolFactorySelector;
import io.lnk.protocol.jackson.JacksonProtocolFactory;
import io.lnk.protocol.jackson.JacksonSerializer;
import io.lnk.remoting.RemotingCommandTest.SimpleBean;
import io.lnk.remoting.exception.RemotingConnectException;
import io.lnk.remoting.exception.RemotingSendRequestException;
import io.lnk.remoting.exception.RemotingTimeoutException;
import io.lnk.remoting.mina.MinaRemotingClient;
import io.lnk.remoting.mina.MinaRemotingServer;
import io.lnk.remoting.protocol.CommandCode;
import io.lnk.remoting.protocol.RemotingCommand;

public class MinaRemotingServerTest {
    private static RemotingServer remotingServer;
    private static RemotingClient remotingClient;

    public static RemotingServer createRemotingServer() throws InterruptedException {
        ServerConfiguration config = new ServerConfiguration();
        final ProtocolFactory protocolFactory = new JacksonProtocolFactory();
        ProtocolFactorySelector protocolFactorySelector = new LnkProtocolFactorySelector();;
        RemotingServer remotingServer = new MinaRemotingServer(protocolFactorySelector, config);
        remotingServer.registerDefaultProcessor(new CommandProcessor() {
            public RemotingCommand processCommand(RemotingCommand request) {
                JacksonSerializer serializer = new JacksonSerializer();
                SimpleBean simpleBean = serializer.deserialize(SimpleBean.class, request.getBody());
                System.err.println("收到客户端请求 : " + serializer.deserialize(String.class, simpleBean.getAvt()));
                simpleBean.setAvt(serializer.serializeAsBytes("来之服务器的问候" + simpleBean.getName() + ", " + simpleBean.getAge() + " 很好啊"));
                request.setBody(protocolFactory.encode(simpleBean));
                return request;
            }

            @Override
            public boolean tryAcquireFailure(long timeoutMillis) {
                return false;
            }

            @Override
            public void release() {}
        }, Executors.newCachedThreadPool());
        remotingServer.start();
        System.err.println(remotingServer.getServerAddress().getAddress());
        System.err.println(remotingServer.getServerAddress().getHostName());
        System.err.println(remotingServer.getServerAddress().getPort());
        return remotingServer;
    }

    public static RemotingClient createRemotingClient() {
        ClientConfiguration config = new ClientConfiguration();
        ProtocolFactorySelector protocolFactorySelector = new LnkProtocolFactorySelector();
        RemotingClient client = new MinaRemotingClient(protocolFactorySelector, config);
        client.start();
        return client;
    }

    @BeforeClass
    public static void setup() throws InterruptedException {
        remotingServer = createRemotingServer();
        remotingClient = createRemotingClient();
    }

    @AfterClass
    public static void destroy() {
        remotingClient.shutdown();
        remotingServer.shutdown();
    }

    @Test
    public void testInvokeSync() throws InterruptedException, RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException {
        try {
            ProtocolFactory protocolFactory = new JacksonProtocolFactory();
            JacksonSerializer serializer = new JacksonSerializer();
            RemotingCommand command = new RemotingCommand();
            command.setCode(CommandCode.SUCCESS);
            SimpleBean simpleBean = new SimpleBean();
            simpleBean.setName("刘飞");
            simpleBean.setAge(30);
            simpleBean.setAvt(serializer.serializeAsBytes("你好吗-sync"));
            command.setBody(protocolFactory.encode(simpleBean));
            RemotingCommand response = remotingClient.invokeSync("localhost:8888", command, 1000 * 3);
            System.err.println("response : " + response);
            SimpleBean reply = serializer.deserialize(SimpleBean.class, response.getBody());
            System.err.println("reply command response : " + serializer.deserialize(String.class, reply.getAvt()));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    @Test
    public void testInvokeAsync() throws InterruptedException, RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException {
        try {
            final ProtocolFactory protocolFactory = new JacksonProtocolFactory();
            final JacksonSerializer serializer = new JacksonSerializer();
            RemotingCommand command = new RemotingCommand();
            command.setCode(CommandCode.SUCCESS);
            SimpleBean simpleBean = new SimpleBean();
            simpleBean.setName("刘飞");
            simpleBean.setAge(30);
            simpleBean.setAvt(serializer.serializeAsBytes("你好吗-async"));
            command.setBody(protocolFactory.encode(simpleBean));
            final CountDownLatch wait = new CountDownLatch(1);
            remotingClient.invokeAsync("localhost:8888", command, 1000 * 3, new RemotingCallback() {
                @Override
                public void onComplete(ReplyFuture replyFuture) {
                    RemotingCommand response = replyFuture.getResponse();
                    System.err.println("response : " + response);
                    SimpleBean reply = serializer.deserialize(SimpleBean.class, response.getBody());
                    System.err.println("reply command response : " + serializer.deserialize(String.class, reply.getAvt()));
                    wait.countDown();
                }
            });
            wait.await(3, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Test
    public void testInvokeOneway() throws InterruptedException, RemotingConnectException, RemotingTimeoutException, RemotingSendRequestException {
        try {
            ProtocolFactory protocolFactory = new JacksonProtocolFactory();
            JacksonSerializer serializer = new JacksonSerializer();
            RemotingCommand command = new RemotingCommand();
            command.setCode(CommandCode.SUCCESS);
            SimpleBean simpleBean = new SimpleBean();
            simpleBean.setName("刘飞");
            simpleBean.setAge(30);
            simpleBean.setAvt(serializer.serializeAsBytes("你好吗-oneway"));
            command.setBody(protocolFactory.encode(simpleBean));
            remotingClient.invokeOneway("localhost:8888", command);
            Thread.sleep(2000L);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
