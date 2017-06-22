package io.lnk.web;

import java.beans.PropertyEditor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;

import io.lnk.api.URI;
import io.lnk.api.protocol.Serializer;
import io.lnk.lookup.zookeeper.ZooKeeperProvider;
import io.lnk.protocol.jackson.JacksonSerializer;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月19日 下午5:54:35
 */
public class BasicController {
    protected final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());
    @Autowired protected HttpServletRequest request;
    private ConcurrentHashMap<String, ZooKeeperProvider> providers = new ConcurrentHashMap<String, ZooKeeperProvider>();
    private Serializer serializer = new JacksonSerializer();
    protected static final String APPLICATION_JSON = "application/json;charset=UTF-8";

    @InitBinder
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        PropertyEditor editor = new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), true);
        binder.registerCustomEditor(Date.class, editor);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<String> handleException(Throwable ex) {
        return resp(ExceptionUtils.getStackTrace(ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected ResponseEntity<String> resp(Object message, HttpStatus statusCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", APPLICATION_JSON);
        return new ResponseEntity<String>(serializer.serializeAsString(message), headers, statusCode);
    }
    
    protected ZooKeeperProvider getProvider(String uri) {
        ZooKeeperProvider provider = this.providers.get(uri);
        if (provider == null) {
            provider = new ZooKeeperProvider(URI.valueOf(uri));
            this.providers.put(uri, provider);
        }
        return provider;
    }
}
