package io.lnk.config.ctx.ns;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.lnk.config.ctx.utils.ByteUtil;
import io.lnk.config.ctx.utils.HexUtil;

/**
 * 属性值加密工具(临时方案，后续由配置系统负责加解密)
 */
public final class NsCryptoUtil {
    /**
     * 加密密钥 20****03 x 2 + 30****02 x 2
     */
    private static final byte[] SECURITY_KEY = new byte[] {32, 18, 9, 3, 32, 18, 9, 3, 48, -112, 33, 2, 48, -112, 33, 2};

    /**
     * 加密数据
     */
    public static final String encrypt(String data) {
        if (data == null) {
            return null;
        }

        byte[] encData = tdesEncrypt(data.getBytes(com.google.common.base.Charsets.UTF_8), SECURITY_KEY, true);
        return HexUtil.encodeHex(encData);
    }

    /**
     * 解密数据
     */
    public static final String decrypt(String encData) {
        if (encData == null) {
            return null;
        }

        byte[] plainData = tdesDecrypt(HexUtil.decodeHex(encData), SECURITY_KEY, true);
        return new String(plainData, com.google.common.base.Charsets.UTF_8);
    }

    /**
     * 3DES加密
     * 
     * @param msgInBCD 待加密数据
     * @param keyInBCD 秘钥明文
     * @param padding 是否使用填充模式
     * @return 加密后的数据
     */
    public static byte[] tdesEncrypt(byte[] msgInBCD, byte[] keyInBCD, boolean padding) {
        try {
            return initTdesCipher(keyInBCD, Cipher.ENCRYPT_MODE, padding).doFinal(msgInBCD);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 3DES解密(padding为false)
     * 
     * @param msgInBCD 加密数据
     * @param keyInBCD 秘钥明文
     * @param padding 加密数据是否使用填充模式
     * @return 解密后的数据
     */
    public static byte[] tdesDecrypt(byte[] msgInBCD, byte[] keyInBCD, boolean padding) {
        try {
            return initTdesCipher(keyInBCD, Cipher.DECRYPT_MODE, padding).doFinal(msgInBCD);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 初始化3DES秘钥
     */
    private static Cipher initTdesCipher(byte[] key, int mode, boolean padding) throws Exception {
        Cipher cipher = null;
        if (padding) {
            cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");

        } else {
            cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        }

        SecretKey secretKey = new SecretKeySpec(format3DesKey(key), "DESede");
        cipher.init(mode, secretKey);

        return cipher;
    }

    /**
     * 格式化3DES秘钥
     */
    private static byte[] format3DesKey(byte[] rawKey) {
        if (rawKey.length == 8) {
            return ByteUtil.concat(rawKey, rawKey, rawKey);

        } else if (rawKey.length == 16) {
            return ByteUtil.concat(rawKey, ByteUtil.getBytes(rawKey, 0, 8));

        } else if (rawKey.length == 24) {
            return rawKey;

        } else {
            throw new RuntimeException("Invalid length of 3DES key, length=" + rawKey.length);
        }
    }

    private NsCryptoUtil() {}

    public static void main(String[] args) {
        System.out.println("andpayapp sec : [" + encrypt("AahbBhVtwK63imwDLbIjr6Gi3JrFgPr3Kk") + "]");
        System.out.println("andpays3 sec : [" + encrypt("jfdev123") + "]");

        System.out.println("andpays3 : [" + decrypt("AahbBhVtwK63imwDLbIjr6Gi3JrFgPr3Kk") + "]");
    }
}
