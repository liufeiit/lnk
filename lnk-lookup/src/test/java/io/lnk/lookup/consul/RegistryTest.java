package io.lnk.lookup.consul;

import io.lnk.api.Address;
import io.lnk.api.URI;
import io.lnk.lookup.consul.ConsulLookupRegistry;

public class RegistryTest {
	public static void main(String[] argv) {
		ConsulLookupRegistry module = new ConsulLookupRegistry(URI.valueOf("consul://10.100.174.22:8500"));
		Address addr = new Address("127.0.0.1:8084");
		module.registry("test-group", "RegistryModuleTest", "1.0.0", 0, addr);
		while(true) {
			try {
				Thread.sleep(2000);
				System.out.println("test------");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
