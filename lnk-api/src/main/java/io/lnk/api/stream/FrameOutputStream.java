package io.lnk.api.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年8月4日 下午7:35:11
 */
public class FrameOutputStream extends OutputStream {
    private final OutputStream out;

    public FrameOutputStream(OutputStream out) {
        super();
        this.out = out;
    }

    private void writeFrame(byte[] b, int off, int length) throws IOException {
        byte[] lenData = Frame.toBytes(length);
        this.out.write(lenData);
        this.out.write(b, off, length);
    }

    @Override
    public void write(int b) throws IOException {
        throw new IOException("unsupport this write method.");
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.writeFrame(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.writeFrame(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
    }

    @Override
    public void close() throws IOException {
        this.out.close();
    }
}
