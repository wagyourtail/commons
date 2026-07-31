package xyz.wagyourtail.commons.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class IOUtils {

    public static void transferTo(SeekableByteChannel in, OutputStream os) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8096);
        while (true) {
            int read = in.read(buffer);
            if (read == -1) {
                break;
            }
            os.write(buffer.array(), 0, read);
            if (in.position() == in.size()) {
                break;
            }
            buffer.clear();
        }
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

}
