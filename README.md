# lnk

Lnk RPC是一款基于Netty实现RPC通讯协议，支持同步，异步和异步回调三种RPC调用方式，支持参数和返回值多态。支持多种负载均衡方式，支持调用流量控制，支持zookeeper和consul等服务注册发现方式，服务端口支持开发人员，运维人员配置以及动态分配，支持服务依赖关系梳理以及调用链路跟踪。支持spring配置。在服务端通过分组策略将来自不同组别的请求处理资源隔离，该思路借鉴与RocketMQ的实现思想。

+ 使用MQ实现的RPC特性：

  - 1.可以把请求的压力保存到MQ，逐渐释放出来，让处理者按照自己的节奏来处理。
  - 2.由于MQ本身具有的缓冲性质，可以在应对高并发的时候保证请求不丢失。
  - 3.异步单向的消息，不需要等待消息处理的完成，故原生支持异步调用模型。
  - 4.无需服务注册中心和负载均衡的概念，完全依赖MQ来完成。
  - 5.引入一下新的MQ结点，系统的可靠性会受MQ结点的影响。
  - 6.对于有同步返回需求，用MQ则变得麻烦。
  - 7.因为AMQP协议面向的场景主要是高可用性的场景，所以其服务端的实现略复杂，并且协议数据比直接tcp的裸数据大不少，另外无论是服务端还是客户端都存在着成对的拆包和封包的过程。

+ 基于Netty等通讯框架实现的RPC的特性：

  - 1.请求压力会直接传递到服务端，需要熔断，限流，降级，超时等服务治理策略。
  - 2.需要有注册中心和负载均衡策略。
  - 3.支持同步和异步调用。
  - 4.性能比较高。

+ 综上所述：

  - 由于MQ的稳定性依然成为我们目前系统稳定性和可靠性的最大障碍，而RabbitMQ的优化定制难度较高，所以寻找一款可替代的MQ服务或者去除MQ结点成为本项目的主题，在对市面上MQ的调研之后，我们决定选择后者方案，因为基于点对点模型的RPC解决方案目前来说比较成熟，可以借鉴学习的产品和思路比较多，很容易打造一款适合我们自身业务特性的RPC产品。

+ Lnk RPC产品所支持的功能

  - 1.同步调用
  - 2.异步调用
  - 3.异步组播调用
  - 4.异步结果回调
  - 5.支持服务多版本
  - 6.支持入参和返回值多态
  - 7.支持异常
  - 8.支持RPC服务序列化（实现异步回调的基础）



# * 使用Spring进行配置

+ Server端配置

  - Server端全部配置
  
  
	``<lnk:server id="paymentServer" client="paymentClient" listen-port="8888" worker-threads="20" 
		selector-threads="15" channel-maxidletime-seconds="120" 
		socket-sndbuf-size="65535" socket-rcvbuf-size="65535" 
		pooled-bytebuf-allocator-enable="true" default-worker-processor-threads="10" 
		default-executor-threads="8" use-epoll-native-selector="false">
		<lnk:application app="biz-pay-bgw-payment-srv" type="jar"/>
		<lnk:registry address="zk://127.0.0.1:2181"/>
		<lnk:flow-control type="semaphore" permits="10000"/>
		<lnk:tracker type="logger"/>
		<lnk:bind>
			<lnk:service-group service-group="biz-pay-bgw-payment.srv" 
				worker-threads="30"/>
			<lnk:service-group service-group="biz-pay-bgw-payment.router.srv" 
				worker-threads="30"/>
		</lnk:bind>
	</lnk:server>``
	
	
  - Server端简单配置
    
    
	``<lnk:server id="paymentServer" client="paymentClient">
		<lnk:application app="biz-pay-bgw-payment-srv"/>
		<lnk:registry address="zk://10.100.156.26:2181"/>
		<lnk:flow-control type="semaphore" permits="10000000"/>
		<lnk:tracker type="logger"/>
		<lnk:bind>
			<lnk:service-group service-group="biz-pay-bgw-payment.srv" 
				worker-threads="10"/>
			<lnk:service-group service-group="biz-pay-bgw-payment.router.srv" 
				worker-threads="10"/>
		</lnk:bind>
	</lnk:server>``
    
    
  - Server端极简配置
    
    
	``<lnk:server id="paymentServer" client="paymentClient">
		<lnk:application app="biz-pay-bgw-payment-srv"/>
		<lnk:registry address="zk://127.0.0.1:2181"/>
	</lnk:server>``
    
    
  - 配置项说明

	client：依赖的client客户端配置，主要用于服务回调支持

	listen-port：配置服务监听端口，可选配置，该配置项优先使用开发人员配置，如果没有配置或者端口被占用，框架会通过系统参数获取当前应用的分配端口，如果系统参数没有配置或者系统参数配置分配的端口被占用的话，框架会自动分配一个当前机器未使用的端口。
	
	worker-threads：Netty服务端事件处理线程池大小，默认为10.
	
	selector-threads：Netty服务NIO selector事件处理线程池大小，默认为5
	
	channel-maxidletime-seconds：Netty服务端连接通道最大空闲时间，单位为秒，默认为120秒
	
	socket-sndbuf-size：服务端socket发送数据大小，默认为65535
	
	socket-rcvbuf-size：服务端socket接收数据大小，默认为65535
	
	pooled-bytebuf-allocator-enable：是否开启Direct Buffer选项，开启之后内核缓冲区的内容直接写入了Channel，这样减少了数据拷贝。默认为true
	
	default-worker-processor-threads：服务端默认消息处理线程池的大小，用于处理一些未分组的请求。默认为10
	
	default-executor-threads：服务端默认线程池大小，主要用于处理异步回调事件。默认为8
	
	use-epoll-native-selector：是否使用操作系统EPoll selector，默认false。
	
	application子节点主要是标示应用名称和应用类型，主要用于做服务调用链路跟踪和应用以来关系梳理。app属性标示应用名称，type标示应用类型分为jar和war类型。
	
	registry子节点主要标示服务注册中心类型和地址，用于server端注册自己的服务调用地址和端口，目前支持zookeeper和consul类型的注册中心，address标示注册中心地址
	
	flow-control子节点主要标示流量控制单元，目前只支持使用semaphore信号量实现的流量控制。
	
	tracker子节点主要标示链路跟踪单元，目前只支持日志跟踪，后续接入cat和另外开发一套梳理应用关系展示和调用链路的web系统。
	
	bind子节点主要是用于将服务端的服务划分为不同的组别，不同的组别使用自身组别的线程池，是的各个组别对外提供服务的线程等资源相互隔离。service-group标示组别名称，worker-threads标示改组请求处理线程池大小。默认为10.
    
    ***
    
+ Client端配置

  - Client端全部配置
    
    
	``<lnk:client id="paymentClient" worker-threads="4" connect-timeout-millis="3000" 
		channel-maxidletime-seconds="120" socket-sndbuf-size="65535" 
		socket-rcvbuf-size="65535" default-executor-threads="4">
		<lnk:application app="biz-pay-bgw-payment-srv" type="jar"/>
		<lnk:lookup address="zk://127.0.0.1:2181"/>
		<lnk:flow-control type="semaphore" permits="1000"/>
		<lnk:load-balance type="hash"/>
	</lnk:client>``
	
	
  - Client端简单配置
	
	
	``<lnk:client id="paymentClient">
		<lnk:application app="biz-pay-bgw-payment-srv"/>
		<lnk:lookup address="zk://127.0.0.1:2181"/>
		<lnk:flow-control type="semaphore" permits="1000"/>
		<lnk:load-balance type="hash"/>
	</lnk:client>``
	
	
  - Client端极简配置
    
    
	``<lnk:client id="paymentClient">
		<lnk:application app="biz-pay-bgw-payment-srv"/>
		<lnk:lookup address="zk://127.0.0.1:2181"/>
		<lnk:load-balance type="hash"/>
	</lnk:client>``
	
	
  - 配置项说明
  
	worker-threads：Netty客户端事件处理线程池大小。默认为4.
	
	connect-timeout-millis：客户端连接超时事件，单位毫秒。默认3000毫米即3秒。
	
	channel-maxidletime-seconds：客户端连接通道最大空闲时间，单位秒，默认120秒。
	
	socket-sndbuf-size：客户端socket发送数据大小，默认为65535
	
	socket-rcvbuf-size：客户端socket接收数据大小，默认为65535
	
	default-executor-threads：客户端默认线程池大小，默认为4.
	
	application子节点主要是标示应用名称和应用类型，主要用于做服务调用链路跟踪和应用以来关系梳理。app属性标示应用名称，type标示应用类型分为jar和war类型。
	
	lookup子节点主要标示服务发现中心类型和地址，用于client端发现自己的依赖的服务调用地址和端口，目前支持zookeeper和consul类型的注册中心，address标示注册中心地址
	
	flow-control子节点主要标示流量控制单元，目前只支持使用semaphore信号量实现的流量控制。
	
	metrics子节点主要是用于配置上报服务治理相关数据使用的influxdb的相关配置。
	
	load-balance子节点表示选择一组服务调用地址的算法，type指定算法类型，目前支持 hash一致性hash算法，random随机算法，roundrobin轮询算法和本地优先算法。
	
	***
    
# * Java代码中注解配置

	@LnkService(group = "biz-pay-bgw-payment.srv")
	public interface AuthService {
	    @LnkMethod(type = InvokeType.SYNC, timeoutMillis = 3000L)
	    AuthResponse auth(AuthRequest request) throws AppBizException;
	    
	    @LnkMethod(type = InvokeType.SYNC, timeoutMillis = 3000L)
	    AuthResponse auth_poly(AuthRequest request) throws AppBizException;
	}
	
	// 标记服务发布的版本
	@LnkServiceVersion(version = "2.0.0")
	public class V2AuthService implements AuthService {
	
	    @Override
	    public AuthResponse auth(AuthRequest request) throws AppBizException {
	        AuthResponse response = new AuthResponse();
	        response.setGateId("0312");
	        response.setGateRespCode("TXN.000");
	        response.setGateRespMsg("V2 : " + request);
	        response.setTxnId(request.getTxnId());
	        return response;
	    }
	
	    @Override
	    public AuthResponse auth_poly(AuthRequest request) throws AppBizException {
	        // 多态的使用
	        PolyAuthResponse response = new PolyAuthResponse();
	        response.setGateId("0104");
	        response.setGateRespCode("TXN.000");
	        response.setGateRespMsg("V2 : " + request);
	        response.setTxnId(request.getTxnId());
	        
	        response.setPrincipalId("101");
	        response.setTxnType("00001");
	        response.setProductType("QP");
	        response.setIdType("01");
	        return response;
	    }
	}
	
# 服务端依赖注入
	
	// 注入默认版本的服务代理
	@Lnkwired(localWiredPriority = false)
    AuthService defaultAuthService;
    
    // 注入2.0.0版本的服务代理
    @Lnkwired(version = "2.0.0", localWiredPriority = false)
    AuthService v2AuthService;
  






