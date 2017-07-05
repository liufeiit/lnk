package io.lnk.config.ctx;

import io.lnk.config.ctx.ns.NsCryptoUtil;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2016年6月20日 上午11:41:08
 */
public class ServerMain {
    
    public static void main(String[] args) {
        System.err.println("decrypt : " + NsCryptoUtil.decrypt("A9181DD1BE0CD5B431FC1BDE5D2B0FE4115868E0722544BD"));
    }
    
    public static void main0(String[] args) {
        System.out.println("@@ Encrypt Args : " + args[0]);
        String retVal = NsCryptoUtil.encrypt(args[0]);
        System.out.println("@@ Encrypt Result : " + retVal);
    }
}