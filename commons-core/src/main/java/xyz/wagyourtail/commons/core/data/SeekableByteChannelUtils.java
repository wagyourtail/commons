package xyz.wagyourtail.commons.core.data;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class SeekableByteChannelUtils {

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

}
