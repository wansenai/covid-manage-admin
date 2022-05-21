package com.summer.common.helper;

import org.springframework.core.io.ClassPathResource;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/** 字节、流操作工具类 **/
public final class BytesHelper {
    public static final Charset UTF8 = Charset.forName("UTF-8");
    public static final Charset GBK = Charset.forName("GB18030");
    private static final int EOF = -1, BUF_LENGTH = 8192;
    private BytesHelper() {
    }

    public static ServletInputStream castServletInputStream(InputStream stream) {
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }
            @Override
            public boolean isReady() {
                return false;
            }
            @Override
            public void setReadListener(ReadListener readListener) {}
            @Override
            public int read() throws IOException {
                return stream.read();
            }
        };
    }

    public static String string(final byte[] source) {
        return null == source ? StringHelper.EMPTY : new String(source, UTF8);
    }

    public static String string(final InputStream input){
        return string(input, UTF8);
    }

    public static String gbkString(final InputStream input) {
        return string(input, GBK);
    }

    public static String gbkString(final byte[] source) {
        return null == source ? StringHelper.EMPTY : new String(source, GBK);
    }

    public static byte[] utf8Bytes(final String info) {
        if(StringHelper.isBlank(info)){
            return new byte[0];
        }
        return info.getBytes(UTF8);
    }

    public static byte[] gbkBytes(final String info) {
        if(StringHelper.isBlank(info)){
            return new byte[0];
        }
        return info.getBytes(GBK);
    }

    public static String resource2String(final String resource) {
        try {
            return BytesHelper.string(new ClassPathResource(resource).getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("resource to string error", e);
        }
    }

    public static byte[] resource2Bytes(final String resource) {
        try {
            return toByteArray(new ClassPathResource(resource).getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("resource to string error", e);
        }
    }

    public static InputStream toStream(final String input) {
        return new ByteArrayInputStream(utf8Bytes(input));
    }

    public static InputStream toStream(byte[] buf) {
        return new ByteArrayInputStream(null == buf ? new byte[0] : buf);
    }

    public static byte[] toByteArray(final InputStream is) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[BUF_LENGTH];
            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("InputStream to ByteArray error", e);
        }finally {
            close(out);
        }
    }

    public static InputStream toGbkStream(final String input) {
        return new ByteArrayInputStream(gbkBytes(input));
    }

    public static int copy(InputStream input, OutputStream output) {
        try {
            long count = copyLarge(input, output, new byte[BUF_LENGTH]);
            if (count > Integer.MAX_VALUE) {
                return EOF;
            }
            return (int) count;
        } catch (Exception e) {
            throw new RuntimeException("copy input to output error", e);
        }
    }

    public static void close(final AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ioe) {
            throw new RuntimeException("close stream error: ", ioe);
        }
    }

    private static String string(final InputStream input, final Charset charset) {
        final StringWriter sw = new StringWriter();
        final InputStreamReader in = new InputStreamReader(input, charset);
        try {
            copyLarge(in, sw, new char[BUF_LENGTH]);
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("input to string error", e);
        } finally {
            close(in);
            close(sw);
        }
    }

    private static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count = 0; int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    private static void copyLarge(final Reader input, final Writer output, final char[] buffer) throws IOException {
        int n; while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }
}
