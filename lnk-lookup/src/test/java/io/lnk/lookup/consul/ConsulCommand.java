package io.lnk.lookup.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

import io.lnk.lookup.consul.ConsulConstants;
import io.lnk.lookup.consul.ConsulUtils;

public class ConsulCommand {
	public static void main(String[] argv) {
		ConsulClient client = new ConsulClient("10.100.174.22:8500");
		// String cmd =
		// "{\"rule\":{\"defaultProperties\":{\"fallbackenable\":\"true\",\"corePoolSize\":40},\"serviceIds\":[{\"serviceId\":\"pay_2015\",\"properties\":{\"executionTimeoutInMilliseconds\":1,\"fallbackIsolationSemaphoreMaxConcurrentRequests\":1,\"fallbackEnable\":true,\"circuitBreakerEnabled\":true,\"circuitBreakerRequestVolumeThreshold\":1,\"circuitBreakerSleepWindowInMilliseconds\":1,\"circuitBreakerErrorThresholdPercentage\":1,\"circuitBreakerForceOpen\":false,\"circuitBreakerForceClosed\":true,\"executionTimeoutEnabled\":true,\"requestCacheEnabled\":true}},{\"serviceId\":\"test_new\"}]},\"serviceGroup\":\"test-group\"}";
		String cmd = "{\"rule\":{\"defaultProperties\":{\"keepAliveTime\":1,\"corePoolSize\":40,\"queueSizeRejectionThreshold\":5,\"executionTimeoutInMilliseconds\":1000,\"fallbackIsolationSemaphoreMaxConcurrentRequests\":10,\"fallbackEnabled\":true,\"circuitBreakerEnabled\":true,\"circuitBreakerRequestVolumeThreshold\":20,\"circuitBreakerSleepWindowInMilliseconds\":5000,\"circuitBreakerErrorThresholdPercentage\":50,\"circuitBreakerForceOpen\":false,\"circuitBreakerForceClosed\":true,\"executionTimeoutEnabled\":true,\"executionTimeoutInMilliseconds\":1000},\"serviceIds\":[{\"serviceId\":\"com.ly.fn.inf.lnk.demo.HelloService\",\"properties\":{\"executionTimeoutInMilliseconds\":1000,\"fallbackIsolationSemaphoreMaxConcurrentRequests\":10,\"fallbackEnabled\":true,\"circuitBreakerEnabled\":true,\"circuitBreakerRequestVolumeThreshold\":20,\"circuitBreakerSleepWindowInMilliseconds\":5000,\"circuitBreakerErrorThresholdPercentage\":50,\"circuitBreakerForceOpen\":false,\"circuitBreakerForceClosed\":true,\"executionTimeoutEnabled\":true,\"executionTimeoutInMilliseconds\":1000}},{\"serviceId\":\"com.ly.fn.inf.lnk.TestService\"}]},\"serviceGroup\":\"biz-pay-bgw-payment.srv\"}";
		String group = "test-group";
		try {
			client.setKVValue(ConsulConstants.CONSUL_LNK_COMMAND + ConsulUtils.convertGroupToServiceName(group), cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Response<GetValue> response = client
					.getKVValue(ConsulConstants.CONSUL_LNK_COMMAND + ConsulUtils.convertGroupToServiceName(group));
			GetValue value = response.getValue();
			String command = "";
			if (value == null) {
				System.out.println("no command in group: " + group);
			} else if (value.getValue() != null) {
				command = value.getDecodedValue();
			}
			System.out.print(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
