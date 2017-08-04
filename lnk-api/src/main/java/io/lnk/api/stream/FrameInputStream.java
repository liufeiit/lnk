package io.lnk.api.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年8月4日 下午7:52:22
 */
public class FrameInputStream extends InputStream {
    private final InputStream in;

    public FrameInputStream(InputStream in) {
        super();
        this.in = in;
    }

    /**
     * 读取Frame字节块。
     * 
     * @return null表示达到字节流尾部。
     */
    public byte[] readFrame() throws IOException {
        byte[] lenData = new byte[Frame.FRAME_SIZE];
        int i = this.in.read(lenData, 0, Frame.FRAME_SIZE);
        if (i == -1) {
            return null;
        }
        if (i != Frame.FRAME_SIZE) {
            throw new IOException("can't read the frame length.");
        }
        int length = Frame.toLength(lenData);
        byte[] data = new byte[length];
        i = this.in.read(data, 0, length);
        if (i != length) {
            throw new IOException("can't read the frame body, excepted=[" + length + "], actual=[" + i + "].");
        }
        return data;
    }

    @Override
    public int read() throws IOException {
        throw new IOException("unsupport this read method.");
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }
}
