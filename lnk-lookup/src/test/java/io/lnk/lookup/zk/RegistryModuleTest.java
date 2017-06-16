package io.lnk.lookup.zk;

import io.lnk.api.Address;
import io.lnk.api.URI;
import io.lnk.lookup.zk.ZokLookupRegistry;

public class RegistryModuleTest {
    public static void main(String[] argv) {
        ZokLookupRegistry module = new ZokLookupRegistry(URI.valueOf("zk://10.100.157.28:2181"));
        Address addr = new Address("127.0.0.1:8081");
        module.registry("test-group", "RegistryModuleTest", "1.0.0", 0, addr);
        module.lookup("test-group", "RegistryModuleTest", "1.0.0", 0);
        module.unregistry("test-group", "RegistryModuleTest", "1.0.0", 0);
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            module.dumpCachedServers();
        }
    }
}
