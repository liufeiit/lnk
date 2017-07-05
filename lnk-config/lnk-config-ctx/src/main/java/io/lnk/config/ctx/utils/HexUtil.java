package io.lnk.config.ctx.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * HEX工具类
 */
public final class HexUtil {
    /**
     * 将HEX字符串转换为Byte数组
     */
    public static byte[] decodeHex(String hex) {
        return decodeHex(hex, '0');
    }

    /**
     * 将HEX字符串转换为Byte数组
     */
    public static byte[] decodeHex(String hex, char padChar) {
        if (hex == null) {
            return null;
        }

        if (hex.length() % 2 == 1) {
            hex += padChar;
        }

        try {
            return Hex.decodeHex(hex.toCharArray());
        } catch (DecoderException e) {
            throw new RuntimeException("Decode hex error", e);
        }
    }

    /**
     * 将数组转换成HEX字符串
     */
    public static String encodeHex(byte[] bytes) {
        return encodeHex(bytes, false);
    }

    /**
     * 将数组转换成HEX字符串
     */
    public static String encodeHex(byte[] bytes, boolean toLowerCase) {
        if (bytes == null) {
            return null;
        }

        return new String(Hex.encodeHex(bytes, toLowerCase));
    }

    private HexUtil() {}
}
