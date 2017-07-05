package io.lnk.config.ctx.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;

import org.springframework.util.StringUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月5日 上午10:35:01
 */
public class FileUtils {

    public static class FileReadHandler {
        private LineNumberReader reader;
        private InputStream in;

        public LineNumberReader getReader() {
            return reader;
        }

        public InputStream getInputStream() {
            return in;
        }
    }

    public static class FileWriteHandler {
        private PrintWriter writer;
        private OutputStream out;

        public PrintWriter getWriter() {
            return writer;
        }

        public OutputStream getOutputStream() {
            return out;
        }
    }

    public static FileReadHandler openForRead(File file, String encoding) {
        FileInputStream fin = null;

        try {
            FileReadHandler handler = new FileReadHandler();
            InputStreamReader streamReader;
            fin = new FileInputStream(file);

            if (encoding == null) {
                encoding = System.getProperty("file.encoding");
            }

            if (encoding != null) {
                streamReader = new InputStreamReader(fin, encoding);
            } else {
                streamReader = new InputStreamReader(fin);
            }

            handler.reader = new LineNumberReader(streamReader);
            handler.in = fin;

            return handler;
        } catch (Throwable e) {
            if (fin != null) {
                try {
                    fin.close();
                } catch (Throwable e2) {
                }
            }

            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static FileReadHandler openForRead(InputStream in, String encoding) {
        try {
            FileReadHandler handler = new FileReadHandler();
            InputStreamReader streamReader;

            if (encoding == null) {
                encoding = System.getProperty("file.encoding");
            }

            if (encoding != null) {
                streamReader = new InputStreamReader(in, encoding);
            } else {
                streamReader = new InputStreamReader(in);
            }

            handler.reader = new LineNumberReader(streamReader);
            handler.in = in;

            return handler;
        } catch (Throwable e) {
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable e2) {
                }
            }

            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static FileReadHandler openForRead(File file) {
        return openForRead(file, null);
    }

    public static FileWriteHandler openForWrite(File file, String encoding) {
        FileOutputStream fos = null;

        try {
            FileWriteHandler handler = new FileWriteHandler();
            OutputStreamWriter streamWriter;
            fos = new FileOutputStream(file);

            if (encoding == null) {
                encoding = System.getProperty("file.encoding");
            }

            if (encoding != null) {
                streamWriter = new OutputStreamWriter(fos, encoding);
            } else {
                streamWriter = new OutputStreamWriter(fos);
            }

            handler.writer = new PrintWriter(streamWriter);
            handler.out = fos;
            return handler;
        } catch (Throwable e) {
            if (fos != null) {

                try {
                    fos.close();
                } catch (Throwable e2) {
                }
            }

            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void printf(FileWriteHandler handler, String format, Object... args) {
        handler.writer.printf(format, args);
    }

    public static void printf(File file, String format, Object... args) {
        FileWriteHandler handler = openForWrite(file);
        try {
            printf(handler, format, args);
        } finally {
            close(handler);
        }
    }

    public static FileWriteHandler openForWrite(File file) {
        return openForWrite(file, null);
    }

    public static String readLine(FileReadHandler handler) {
        try {
            return handler.reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String readLine(File file, String encoding) {
        FileReadHandler handler = openForRead(file, encoding);
        try {
            return readLine(handler);
        } finally {
            close(handler);
        }
    }

    public static String readLine(File file) {
        return readLine(file, null);
    }

    public static void copyFile(File src, File target) {
        InputStream inStream = null;
        OutputStream outStream = null;

        try {
            inStream = new FileInputStream(src);
            outStream = new FileOutputStream(target);

            byte[] buffer = new byte[1024];

            int length;
            // copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            inStream.close();
            outStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void close(FileReadHandler handler) {
        closeQuietly(handler.reader);
        closeQuietly(handler.in);
    }

    public static void close(FileWriteHandler handler) {
        closeQuietly(handler.writer);
        closeQuietly(handler.out);
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            // Swallow the exception
        }
    }

    public static String getFileNameExtension(String fileName) {
        int idx = fileName.lastIndexOf(".");
        if (idx <= 0) {
            return "";
        }

        int idx2 = fileName.lastIndexOf("/");
        if (idx < idx2) {
            return "";
        }

        return fileName.substring(idx + 1);
    }

    public static String changeFileNameExtension(String filename, String ext) {
        StringBuilder sb = new StringBuilder();

        if (StringUtils.isEmpty(getFileNameExtension(filename)) == false) {
            sb.append(filename.substring(0, filename.lastIndexOf(".")));
        } else {
            sb.append(filename);
        }

        sb.append(".");
        sb.append(ext);

        return sb.toString();
    }

    public static void loadProperties(Properties props, File file) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            props.load(fin);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (Throwable e) {
                }
            }
        }
    }
}
