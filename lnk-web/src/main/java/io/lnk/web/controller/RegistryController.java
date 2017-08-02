package io.lnk.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import io.lnk.api.Address;
import io.lnk.web.interceptor.Authorization;
import io.lnk.web.interceptor.ContentType;
import io.lnk.web.interceptor.Permissions;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月19日 下午5:48:54
 */
@Controller
@RequestMapping(value = "/lnk")
public class RegistryController extends BasicController {
    
    @RequestMapping(value = "/registry", method = RequestMethod.GET)
    public ModelAndView selector() {
        ModelAndView mv = this.respMov("registry");
        return mv;
    }

    @RequestMapping(value = "/registry/servers/list", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> serverList(@RequestParam("serviceId") String serviceId) throws Throwable {
        Map<String, List<String>> servers = this.registryService.getServerList(serviceId);
        return resp(servers, HttpStatus.OK);
    }

    @RequestMapping(value = "/registry/servers/single", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> serverList(@RequestParam("serviceId") String serviceId, @RequestParam("version") String version, @RequestParam("protocol") int protocol) throws Throwable {
        List<String> serverList = this.registryService.getServerList(serviceId, version, protocol);
        return resp(serverList, HttpStatus.OK);
    }

    @RequestMapping(value = "/registry/servers/reg", method = RequestMethod.POST)
    @ResponseBody
    @Authorization(contentType = ContentType.JSON, permission = Permissions.REGISTRY)
    public ResponseEntity<String> registryServer(@RequestParam("serviceId") String serviceId, @RequestParam("version") String version, @RequestParam("protocol") int protocol, @RequestParam("server") String server) throws Throwable {
        this.registryService.registry(serviceId, version, protocol, new Address(server));
        List<String> serverList = this.registryService.getServerList(serviceId, version, protocol);
        return resp(serverList, HttpStatus.OK);
    }

    @RequestMapping(value = "/registry/servers/unreg", method = RequestMethod.POST)
    @ResponseBody
    @Authorization(contentType = ContentType.JSON, permission = Permissions.REGISTRY)
    public ResponseEntity<String> unregistryServer(@RequestParam("serviceId") String serviceId, @RequestParam("version") String version, @RequestParam("protocol") int protocol, @RequestParam("server") String server) throws Throwable {
        this.registryService.unregistry(serviceId, version, protocol, new Address(server));
        List<String> serverList = this.registryService.getServerList(serviceId, version, protocol);
        return resp(serverList, HttpStatus.OK);
    }
}
