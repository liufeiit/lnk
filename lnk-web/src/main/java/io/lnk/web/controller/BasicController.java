package io.lnk.web.controller;

import java.beans.PropertyEditor;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.servlet.ModelAndView;

import io.lnk.api.protocol.Serializer;
import io.lnk.protocol.jackson.JacksonSerializer;
import io.lnk.web.service.RegistryService;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月19日 下午5:54:35
 */
public class BasicController {
    protected final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());
    protected static final String APPLICATION_JSON = "application/json;charset=UTF-8";
    protected static final String JID = "__JLNKSESSIONID";
    protected static final Serializer serializer = new JacksonSerializer();
    @Autowired protected HttpServletRequest request;
    @Autowired protected HttpServletResponse response;
    @Autowired protected RegistryService registryService;

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
    
    protected ModelAndView respMov(String viewName) {
        ModelAndView mov = new ModelAndView(viewName);
        return mov;
    }
}
