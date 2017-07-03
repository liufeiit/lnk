package io.lnk.port;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.api.app.Application;
import io.lnk.api.port.ServerPortAllocator;
import io.lnk.api.utils.NetUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午7:15:42
 */
public class DefaultServerPortAllocator implements ServerPortAllocator {
    private static final Logger log = LoggerFactory.getLogger(DefaultServerPortAllocator.class.getSimpleName());
    private static final String ALLOC_PORT_HOME = "lnk.alloc.port.home";
    private Properties env;
    private String instance;

    public DefaultServerPortAllocator() {
        super();
        env = new Properties(System.getProperties());
        Map<String, String> senv = System.getenv();
        for (Map.Entry<String, String> e : senv.entrySet()) {
            env.setProperty(e.getKey(), e.getValue());
        }
        instance = StringUtils.defaultString(env.getProperty("app.instance"), "1");
    }


    @Override
    public int selectPort(int expectListenPort, Application application) {
        if (NetUtils.isAvailable(expectListenPort)) {
            return expectListenPort;
        }
        String portKey = application.getApp() + "." + instance + ".port";
        String allocPortHome = System.getProperty(ALLOC_PORT_HOME);
        if (StringUtils.isBlank(allocPortHome)) {
            allocPortHome = System.getProperty("user.home") + "/.lnk_alloc_port";
        }
        File appPortFile = new File(allocPortHome + "/" + application + ".properties");
        if (appPortFile.exists()) {
            try {
                Properties portProps = new Properties();
                portProps.load(new FileReader(appPortFile));
                String portString = portProps.getProperty(portKey);
                if (StringUtils.isBlank(portString)) {
                    int port = NetUtils.getAvailablePort(10024);
                    portProps.setProperty(portKey, port + "");
                    portProps.store(new FileWriter(appPortFile), application + " port alloc.");
                    return port;
                }
                int port = NumberUtils.toInt(portString, -1);
                if (NetUtils.isAvailable(port) == false) {
                    port = NetUtils.getAvailablePort(10029);
                    portProps.setProperty(portKey, port + "");
                    portProps.store(new FileWriter(appPortFile), application + " port alloc.");
                    return port;
                }
                return port;
            } catch (Throwable e) {
                log.error("selectPort loading alloc port file Error.", e);
            }
        }
        try {
            appPortFile.getParentFile().mkdirs();
            boolean createPortFile = appPortFile.createNewFile();
            if (createPortFile == false) {
                appPortFile.delete();
                createPortFile = appPortFile.createNewFile();
                if (createPortFile == false) {
                    return NetUtils.getAvailablePort(10034);
                }
            }
            Properties portProps = new Properties();
            int port = NetUtils.getAvailablePort(10039);
            if (NetUtils.isAvailable(port) == false) {
                port = NetUtils.getAvailablePort(10044);
                portProps.setProperty(portKey, port + "");
                portProps.store(new FileWriter(appPortFile), application + " port alloc.");
                return port;
            }
            portProps.setProperty(portKey, port + "");
            portProps.store(new FileWriter(appPortFile), application + " port alloc.");
            return port;
        } catch (Throwable e) {
            log.error("selectPort create alloc port file Error.", e);
        }
        return NetUtils.getAvailablePort(10049);
    }
}
