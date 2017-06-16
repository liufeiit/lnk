package io.lnk.api.utils;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午9:47:28
 */
public class CorrelationIds {
    
    public static String buildGuid() {
        String uuidString = 
                Long.toHexString(System.nanoTime() >> 2) + "-" + 
                Long.toHexString(System.nanoTime() >> 3) + "-" + 
                Long.toHexString(System.nanoTime() >> 4) + "-" + 
                Long.toHexString(System.nanoTime() >> 5) + "-" + 
                Long.toHexString(System.nanoTime() >> 6);
        String uuid = UUID.fromString(uuidString).toString();
        uuid = StringUtils.upperCase(RandomStringUtils.randomAlphabetic(10) + Thread.currentThread().getId() + StringUtils.replace(uuid, "-", StringUtils.EMPTY));
        return uuid;
    }
}