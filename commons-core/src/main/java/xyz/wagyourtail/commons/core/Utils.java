package xyz.wagyourtail.commons.core;

import xyz.wagyourtail.commons.core.function.IOSupplier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Utils {

    public static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    public static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
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

}
