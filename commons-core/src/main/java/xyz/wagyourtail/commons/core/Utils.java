package xyz.wagyourtail.commons.core;

import lombok.SneakyThrows;
import xyz.wagyourtail.commons.core.function.IOSupplier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private Utils() {
    }

    public static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    @SneakyThrows
    public static void sneakyThrow(Throwable t) {
        throw t;
    }

    public static URL bufferURL(String name, final IOSupplier<InputStream> inputStreamSupplier) throws MalformedURLException {
        return new URL("x-buffer", null, -1, name, new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(final URL u1) {
                return new URLConnection(u1) {
                    @Override
                    public void connect() {
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return inputStreamSupplier.get();
                    }
                };
            }
        });
    }

    public static int getCurrentClassVersion() {
        String version = System.getProperty("java.class.version");
        if (version != null) {
            try {
                return Integer.parseInt(version.split("\\.")[0]);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        throw new UnsupportedOperationException("Unable to determine current class version");
    }

    public static int classVersionToMajorVersion(int version) {
        if (version == /* Opcodes.V1_1 = */ 196653) return 1;
        else return version - /* Opcodes.V1_2 = */ 46 + 2;
    }

    public static int majorVersionToClassVersion(int version) {
        if (version == 1) return /* Opcodes.V1_1 = */ 196653;
        else return version + /* Opcodes.V1_2 = */ 46 - 2;
    }

    public static int nextPowerOf2(int num) {
        return Integer.highestOneBit(num - 1) << 1;
    }

    public static long nextPowerOf2(long num) {
        return Long.highestOneBit(num - 1) << 1;
    }

}
