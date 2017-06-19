package io.lnk.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.lnk.lookup.zookeeper.ZooKeeperProvider;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月19日 下午5:48:54
 */
@Controller
@RequestMapping(value = "/lnk/registry")
public class Navigator extends BasicController {

    @RequestMapping(value = "/ns")
    public ResponseEntity<String> serviceGroupList(@RequestParam("uri") String uri) throws Throwable {
        log.info("serviceGroupList uri={}", uri);
        ZooKeeperProvider provider = super.getProvider(uri);
        List<String> serviceGroupList = provider.getChildren("/lnk");
        return resp(serviceGroupList, HttpStatus.OK);
    }

    @RequestMapping(value = "/ns/{serviceGroup}")
    public ResponseEntity<String> serviceIdList(@RequestParam("uri") String uri, @PathVariable String serviceGroup) throws Throwable {
        log.info("serviceIdList uri={}, serviceGroup={}", uri, serviceGroup);
        ZooKeeperProvider provider = super.getProvider(uri);
        List<String> serviceIdList = provider.getChildren("/lnk/" + serviceGroup);
        return resp(serviceIdList, HttpStatus.OK);
    }

    @RequestMapping(value = "/ns/{serviceGroup}/{serviceId}")
    public ResponseEntity<String> versionList(@RequestParam("uri") String uri, @PathVariable String serviceGroup, @PathVariable String serviceId) throws Throwable {
        log.info("versionList uri={}, serviceGroup={}, serviceId={}", uri, serviceGroup, serviceId);
        ZooKeeperProvider provider = super.getProvider(uri);
        List<String> versionList = provider.getChildren("/lnk/" + serviceGroup + "/" + serviceId);
        return resp(versionList, HttpStatus.OK);
    }

    @RequestMapping(value = "/ns/{serviceGroup}/{serviceId}/{version}")
    public ResponseEntity<String> protocolList(@RequestParam("uri") String uri, @PathVariable String serviceGroup, @PathVariable String serviceId, @PathVariable String version) throws Throwable {
        log.info("protocolList uri={}, serviceGroup={}, serviceId={}, version={}", uri, serviceGroup, serviceId, version);
        ZooKeeperProvider provider = super.getProvider(uri);
        List<String> protocolList = provider.getChildren("/lnk/" + serviceGroup + "/" + serviceId + "/" + version);
        return resp(protocolList, HttpStatus.OK);
    }

    @RequestMapping(value = "/ns/{serviceGroup}/{serviceId}/{version}/{protocol}")
    public ResponseEntity<String> serverList(@RequestParam("uri") String uri, @PathVariable String serviceGroup, @PathVariable String serviceId, @PathVariable String version, @PathVariable int protocol) throws Throwable {
        log.info("serverList uri={}, serviceGroup={}, serviceId={}, version={}, protocol={}", uri, serviceGroup, serviceId, version, protocol);
        ZooKeeperProvider provider = super.getProvider(uri);
        List<String> protocolList = provider.getChildren("/lnk/" + serviceGroup + "/" + serviceId + "/" + version + "/" + protocol + "/servers");
        return resp(protocolList, HttpStatus.OK);
    }
}
