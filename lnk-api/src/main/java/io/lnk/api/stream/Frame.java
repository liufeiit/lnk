package io.lnk.api.stream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Frame结构长度类，采用BIG_ENDIAN字节顺序，用于网络传输
 * 
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年8月4日 下午7:41:34
 */
public class Frame {
    public static int FRAME_SIZE;
    static {
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(Integer.MAX_VALUE);
        FRAME_SIZE = byteBuffer.position();
    }

    public static int toLength(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        return byteBuffer.getInt();
    }

    public static byte[] toBytes(int length) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(FRAME_SIZE);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(length);
        return byteBuffer.array();
    }
}