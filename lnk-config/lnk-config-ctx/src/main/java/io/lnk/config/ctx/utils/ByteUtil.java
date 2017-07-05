package io.lnk.config.ctx.utils;

/**
 * 字节工具类
 */
public class ByteUtil {
    /**
     * HEX大写
     */
    private static final char[] HEX_TABLE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 1个字节 FF 的字节数组
     */
    public static final byte[] B1_0xFF = {(byte) 0xFF};

    /**
     * 1个字节 00 的字节数组
     */
    public static final byte[] B1_0x00 = {0x00};

    /**
     * 获得数组对象包含的元素数量
     * 
     * @param array 数组
     * @return 数组大小，如果array为null，则返回0
     */
    public static int length(byte[] array) {
        return (array == null ? 0 : array.length);
    }

    /**
     * 获得所有数组对象包含的元素总数量
     * 
     * @param arrays 数组集
     * @return 数组集大小，如果arrays为null，则返回0
     */
    public static int length(byte[]... arrays) {
        int len = 0;
        if (arrays != null) {
            for (byte[] array : arrays) {
                if (array != null) {
                    len += array.length;
                }
            }
        }

        return len;
    }

    /**
     * 判断数组对象是否为null或空
     * 
     * @param array 数组
     * @return 是否为null或空
     */
    public static boolean isEmpty(byte[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * 判断数组对象是否不为null或空
     * 
     * @param array 数组
     * @return 是否不为null或空
     */
    public static boolean isNotEmpty(byte[] array) {
        return (array != null && array.length > 0);
    }

    /**
     * 翻转byte数组中的每一位(原字节数组不会被修改)
     * 
     * @param bytes
     * @return
     */
    public static byte[] inverse(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        byte[] newBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            newBytes[i] = (byte) (bytes[i] ^ 0xFF);
        }

        return newBytes;
    }

    /**
     * 拼接字节数组
     * 
     * @param bytesArr 字节数组
     * @return
     */
    public static byte[] concat(byte[]... bytesArr) {
        if (bytesArr == null) {
            return null;
        }

        byte[] newArray = new byte[length(bytesArr)];
        for (int i = 0, offset = 0; i < bytesArr.length; i++) {
            if (bytesArr[i] != null) {
                System.arraycopy(bytesArr[i], 0, newArray, offset, bytesArr[i].length);
                offset += bytesArr[i].length;
            }
        }

        return newArray;
    }

    /**
     * 在字节数组右侧填充指定字节至指定长度
     * 
     * @param bytes
     * @param length
     * @param padByte
     * @return
     */
    public static byte[] rightPad(byte[] bytes, int length, byte padByte) {
        if (bytes == null) {
            return null;

        } else if (bytes.length >= length) {
            return bytes;
        }

        byte[] newBytes = new byte[length];
        System.arraycopy(bytes, 0, newBytes, 0, bytes.length);

        for (int i = bytes.length; i < length; i++) {
            newBytes[i] = padByte;
        }

        return newBytes;
    }

    /**
     * 在根据给定下标从数组中取出最多指定数量的字节
     * 
     * @param bytes 字节数组
     * @param offset 起始下标(偏移), 负数代表从最后一个元素开始倒数，负无穷大时位置为0
     * @param length 最大记录数，正数，当起始下标加length超过数组长度时，length等于数组长度减起始下标
     * @return
     */
    public static byte[] getBytes(byte[] bytes, int offset, int length) {
        if (bytes == null) {
            return null;

        } else if (bytes.length == 0 || offset >= bytes.length || length <= 0) {
            return new byte[0];
        }

        if (offset < 0) {
            offset = Math.max(0, bytes.length + offset);
        }

        if (offset + length > bytes.length) {
            length = bytes.length - offset;
        }

        byte[] subBytes = new byte[length];
        System.arraycopy(bytes, offset, subBytes, 0, length);

        return subBytes;
    }

    /**
     * 返回包含目标数组指定范围[start, length)内元素的数组
     * 
     * @param bytes 字节数组
     * @param start 起始下标(包含), 负数代表从最后一个元素开始倒数，负无穷大时位置为0，正无穷大位置为length
     * @return
     */
    public static byte[] subBytes(byte[] bytes, int start) {
        return subBytes(bytes, start, 0);
    }

    /**
     * 返回包含目标数组指定范围[start, end)内元素的数组
     * 
     * @param bytes 字节数组
     * @param start 起始下标(包含), 负数代表从最后一个元素开始倒数，负无穷大和零时位置为0，正无穷大位置为length
     * @param end 结束下标(不包含), 负数代表从最后一个元素开始倒数，负无穷大时位置为0，零和正无穷大位置为length
     * @return
     */
    public static byte[] subBytes(byte[] bytes, int start, int end) {
        if (bytes == null) {
            return null;
        }

        if (bytes.length == 0) {
            return new byte[0];
        }

        if (start < 0) {
            start = Math.max(0, bytes.length + start); // 负无穷大取0
        }

        if (end <= 0) {
            end = bytes.length + end;
        }

        end = Math.min(end, bytes.length); // 正无穷大取length

        if (start >= bytes.length || end > bytes.length || start >= end) {
            return new byte[0];
        }

        byte[] subBytes = new byte[end - start];
        System.arraycopy(bytes, start, subBytes, 0, end - start);

        return subBytes;
    }

    /**
     * 字节数组之间进行异或(原字节数组不会被修改)
     * 
     * @param bytesArr
     * @return
     */
    public static byte[] xor(byte[]... bytesArr) {
        if (bytesArr == null) {
            return null;
        }

        byte[] data = null;
        for (int i = 0; i < bytesArr.length; i++) {
            byte[] bytes = bytesArr[i];
            if (bytes == null) {
                continue;

            } else if (data == null) {
                data = new byte[bytes.length];
                System.arraycopy(bytes, 0, data, 0, data.length);
                continue;
            }

            if (bytes.length != data.length) {
                throw new IllegalArgumentException("Different length of two byte arrays");
            }

            for (int j = 0; j < data.length; j++) {
                data[j] ^= bytes[j];
            }
        }

        return data;
    }

    /**
     * 字节数组与指定字节进行异或(原字节数组不会被修改)
     * 
     * @param bytes
     * @return
     */
    public static byte[] xor(byte[] bytes, byte aByte) {
        if (bytes == null) {
            return null;
        }

        byte[] data = new byte[bytes.length];
        System.arraycopy(bytes, 0, data, 0, data.length);
        for (int i = 0; i < data.length; i++) {
            data[i] ^= aByte;
        }

        return data;
    }

    /**
     * 将非负长整形转换为字节数组(高位在前)，数组长度根据num大小而定，0-255长度为1，256-65535长度为2，以此类推
     * 
     * @param num 需要转换的数字，负数返回null
     * @return
     */
    public static byte[] parseBytes(long num) {
        if (num < 0) {
            return null;

        } else if (num == 0) {
            return new byte[1]; // 至少1字节
        }

        int length = 1;
        long tmp = num;
        while ((tmp = tmp >> 8) != 0) {
            length++;
        }

        byte[] bytes = new byte[length];
        for (int i = length - 1; i >= 0; i--) {
            bytes[i] = (byte) (num & 0xff);
            num = num >> 8;
        }

        return bytes;
    }

    /**
     * 将非负长整形转换为长度为length的字节数组(高位在前)
     * 
     * @param num 需要转换的数字，负数返回null
     * @param length 设定返回字节数长度，小于等于0返回null; 如果length小于num转换后的实际长度，则只返回length长度的数组(超出部分不进行转换)
     * @return 转换后的字节数组
     */
    public static byte[] parseBytes(long num, int length) {
        if (num < 0 || length <= 0) {
            return null;
        }

        byte[] bytes = new byte[length];
        for (int i = length - 1; i >= 0 && num > 0; i--) {
            bytes[i] = (byte) (num & 0xff);
            num = num >> 8;
        }

        return bytes;
    }

    /**
     * 将字节数组转换成长整形
     * 
     * @param bytes
     * @return
     */
    public static long toLong(byte[] bytes) {
        long num = 0;
        if (bytes != null) {
            for (int i = bytes.length - 1, j = 0; i >= 0; i--, j++) {
                num += (long) (bytes[i] & 0xff) << (j * 8);
            }
        }
        return num;
    }

    /**
     * 将bytes的元素倒序排列
     * 
     * @param bytes
     */
    public static void reverse(byte[] bytes) {
        if (bytes == null) {
            return;
        }

        byte temp;
        for (int i = 0; i < bytes.length / 2; i++) {
            temp = bytes[i];
            bytes[i] = bytes[bytes.length - 1 - i];
            bytes[bytes.length - 1 - i] = temp;
        }
    }

    /**
     * 判断bytes字节数组的前prefix字节数组长度的字节数据是否与prefix字节数组相同
     * 
     * @param bytes
     * @param offset
     * @param prefix
     * @return
     */
    public static boolean startsWith(byte[] bytes, int offset, byte... prefix) {
        if (length(bytes) == 0 || length(prefix) == 0 || prefix.length > bytes.length || offset < 0 || offset + prefix.length > bytes.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (prefix[i] != bytes[i + offset]) {
                return false;
            }
        }

        return true;
    }

    /**
     * 去除bytes字节数组左侧与tirmBytes相同内容的字节数组，如果tirmBytes在左侧重复出现多次则全部去除。
     * 
     * @param bytes
     * @param tirmBytes
     * @return bytes为null或空、tirmBytes为null或空、bytes的长度比tirmBytes的长度小、 未匹配到tirmBytes时直接返回bytes
     */
    public static byte[] tirmLeft(byte[] bytes, byte... tirmBytes) {
        if (length(bytes) == 0 || length(tirmBytes) == 0 || bytes.length < tirmBytes.length) {
            return bytes;
        }

        int offset = 0;
        while (startsWith(bytes, offset, tirmBytes)) {
            offset += tirmBytes.length;
        }

        if (offset == 0) {
            return bytes;
        }

        byte[] newBytes = new byte[bytes.length - offset];
        System.arraycopy(bytes, offset, newBytes, 0, newBytes.length);
        return newBytes;
    }

    /**
     * 去除bytes字节数组右侧与tirmBytes相同内容的字节数组，如果tirmBytes在右侧重复出现多次则全部去除。
     * 
     * @param bytes
     * @param tirmBytes
     * @return bytes为null或空、tirmBytes为null或空、bytes的长度比tirmBytes的长度小、 未匹配到tirmBytes时直接返回bytes
     */
    public static byte[] tirmRight(byte[] bytes, byte... tirmBytes) {
        if (length(bytes) == 0 || length(tirmBytes) == 0 || bytes.length < tirmBytes.length) {
            return bytes;
        }

        int offset = bytes.length;
        while (startsWith(bytes, offset - tirmBytes.length, tirmBytes)) {
            offset -= tirmBytes.length;
        }

        if (offset == bytes.length) {
            return bytes;
        }

        byte[] newBytes = new byte[offset];
        System.arraycopy(bytes, 0, newBytes, 0, newBytes.length);
        return newBytes;
    }

    /**
     * 获取指定下标的字节
     * 
     * @param bytes
     * @param idx
     * @param defByte
     * @return
     */
    public static byte byteAt(byte[] bytes, int idx, byte defByte) {
        if (bytes == null || idx < 0 || idx >= bytes.length) {
            return defByte;
        }

        return bytes[idx];
    }

    /**
     * 查找目标字节数组第1次出现的位置。如果找到则返回从起始到目标开始位置之间所有内容，否则返回null。
     * 
     * @param src
     * @param target
     * @return
     */
    public static byte[] find(byte[] src, byte[] target) {
        return find(src, target, 0, 1);
    }

    /**
     * 从偏移位置开始查找目标字节数组第1次出现的位置。 如果找到则返回从偏移位置到目标开始位置之间所有内容，否则返回null。
     * 
     * @param src
     * @param target
     * @param offset 起始下标(偏移), 负数代表从最后一个元素开始倒数，负无穷大时位置为0
     * @return
     */
    public static byte[] find(byte[] src, byte[] target, int offset) {
        return find(src, target, offset, 1);
    }

    /**
     * 从偏移位置开始查找目标字节数组第N次出现的位置。 如果找到则返回从偏移位置到目标开始位置之间所有内容。 如果目标字节数组未找到或出现次数未达到指定值，则返回null。
     * 
     * @param src
     * @param target
     * @param offset 起始下标(偏移), 负数代表从最后一个元素开始倒数，负无穷大时位置为0
     * @param occurTimes
     * @return
     */
    public static byte[] find(byte[] src, byte[] target, int offset, int occurTimes) {
        int end = indexOf(src, target, offset, occurTimes);
        if (end == -1) {
            return null;
        }

        if (offset < 0) {
            offset = Math.max(0, src.length + offset);
        }

        byte[] val = new byte[end - offset];
        System.arraycopy(src, offset, val, 0, val.length);

        return val;
    }

    /**
     * 查找目标字节数组第1次出现的位置
     * 
     * @param src
     * @param target
     * @return
     */
    public static int indexOf(byte[] src, byte[] target) {
        return indexOf(src, target, 0, 1);
    }

    /**
     * 从指定位置开始查找目标字节数组第1次出现的位置
     * 
     * @param src
     * @param target
     * @param offset 起始下标(偏移), 负数代表从最后一个元素开始倒数，负无穷大时位置为0
     * @return
     */
    public static int indexOf(byte[] src, byte[] target, int offset) {
        return indexOf(src, target, offset, 1);
    }

    /**
     * 从指定位置开始查找目标字节数组出现第N次的位置
     * 
     * @param src
     * @param target
     * @param offset 起始下标(偏移), 负数代表从最后一个元素开始倒数，负无穷大时位置为0
     * @param occurTimes
     * @return
     */
    public static int indexOf(byte[] src, byte[] target, int offset, int occurTimes) {
        if (isEmpty(src) || isEmpty(target) || occurTimes <= 0) {
            return -1;
        }

        if (offset < 0) {
            offset = Math.max(0, src.length + offset);
        }

        if (target.length + offset > src.length) {
            return -1;
        }

        int endPosition = src.length - target.length;
        while (occurTimes-- > 0) {
            int pos = -1;
            next: for (int i = offset; i <= endPosition; i++) {
                for (int j = 0; j < target.length; j++) {
                    if (src[i + j] != target[j]) {
                        continue next;
                    }
                }

                pos = i;
                break;
            }

            if (pos == -1) {
                return -1; // not found
            }

            offset = pos + 1; // found
        }

        return offset - 1; // last pos
    }

    /**
     * 将字节数组转换为HEX字符串
     * 
     * @param data
     * @return
     */
    public static String asHex(byte[] bytes) {
        return asHex(bytes, false);
    }

    /**
     * 将字节数组转换为HEX字符串
     * 
     * @param data
     * @param lowerCase
     * @return
     */
    public static String asHex(byte[] bytes, boolean lowerCase) {
        if (bytes == null) {
            return null;
        }

        int l = bytes.length;
        char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = HEX_TABLE[(0xF0 & bytes[i]) >>> 4];
            out[j++] = HEX_TABLE[0x0F & bytes[i]];
        }

        String hex = new String(out);
        return (lowerCase ? hex.toLowerCase() : hex);
    }

    /**
     * 将HEX转换为字节数组
     * 
     * @param hex
     * @return
     */
    public static byte[] fromHex(String hex) {
        return fromHex(hex, '0');
    }

    /**
     * 将HEX转换为字节数组，当HEX长度为奇数时，在后面填充padHex
     * 
     * @param hex
     * @param padHex 填充字符
     * @return
     */
    public static byte[] fromHex(String hex, char padHex) {
        if (hex == null) {
            return null;
        }

        if ((hex.length() & 1) == 1) {
            hex += padHex;
        }

        char[] data = hex.toUpperCase().toCharArray();
        int len = data.length;

        byte[] out = new byte[len >> 1];
        for (int i = 0, j = 0; j < len; i++) {
            int f = hex2Int(data[j], j) << 4;
            j++;
            f = f | hex2Int(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * 将HEX字符转换为整型
     * 
     * @param hex
     * @param index
     * @return
     */
    private static int hex2Int(char hex, int index) {
        int digit = Character.digit(hex, 16);
        if (digit == -1) {
            throw new RuntimeException(String.format("Illegal hex char %s at hex[%d]", hex, index));
        }

        return digit;
    }

    private ByteUtil() {}
}
