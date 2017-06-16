package io.lnk.lookup.consul;

import io.lnk.api.Address;
import io.lnk.api.URI;
import io.lnk.lookup.consul.ConsulLookupRegistry;

public class RegistryModuleTest {
	public static void main(String[] argv) {
		ConsulLookupRegistry module = new ConsulLookupRegistry(URI.valueOf("consul://10.100.174.22:8500"));
		Address addr = new Address("127.0.0.1:8083");
		module.registry("test-group", "RegistryModuleTest", "1.0.0", 0, addr);

		// module.unregistry("test-group", "RegistryModuleTest", 0, 0);
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			module.lookup("test-group", "RegistryModuleTest", "1.0.0", 0);
		}

	}
}
