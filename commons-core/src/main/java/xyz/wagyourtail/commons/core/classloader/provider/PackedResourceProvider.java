package xyz.wagyourtail.commons.core.classloader.provider;

import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.classloader.ResourceProvider;
import xyz.wagyourtail.commons.core.function.IOSupplier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class PackedResourceProvider implements ResourceProvider {
    private final SeekableByteChannel channel;
    private final Map<String, PositionAndLength> positions;

    public PackedResourceProvider(Path path) throws IOException {
        this(Files.newByteChannel(path));
    }

    public PackedResourceProvider(SeekableByteChannel channel) throws IOException {
        this.channel = channel;
        this.positions = new HashMap<>();
        while (channel.position() < channel.size()) {
            // read name
            int nameLength = getInt(channel);
            int fileLength = getInt(channel);
            String name = getString(channel, nameLength);
            long position = channel.position();
            this.positions.put(name, new PositionAndLength(position, fileLength));
            channel.position(position + fileLength);
        }
    }

    public static PackedResourceProvider ofUri(URI uri) throws IOException {
        if (uri.getScheme().equals("file")) {
            return new PackedResourceProvider(Paths.get(uri));
        }
        // write to temp file in current dir
        Path temp = Files.createTempFile(Paths.get("."), "packed", "classes");
        Files.copy(uri.toURL().openStream(), temp, StandardCopyOption.REPLACE_EXISTING);
        return new PackedResourceProvider(temp);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (positions.containsKey(name)) {
            final PositionAndLength positionAndLength = positions.get(name);
            return Collections.enumeration(Collections.singleton(Utils.bufferURL(name, new IOSupplier<InputStream>() {

                @Override
                public InputStream get() throws IOException {
                    ByteBuffer buffer = ByteBuffer.allocate(positionAndLength.length);
                    synchronized (channel) {
                        channel.position(positionAndLength.position);
                        channel.read(buffer);
                    }
                    buffer.flip();
                    return new ByteArrayInputStream(buffer.array());
                }

            })));
        }
        return Collections.emptyEnumeration();
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            channel.close();
        }
    }

    public static int getInt(SeekableByteChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        channel.read(buffer);
        buffer.flip();
        return buffer.getInt();
    }

    public static byte[] getBytes(SeekableByteChannel channel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        channel.read(buffer);
        buffer.flip();
        return buffer.array();
    }

    public static String getString(SeekableByteChannel channel, int length) throws IOException {
        byte[] bytes = getBytes(channel, length);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static class PositionAndLength {
        private final long position;
        private final int length;

        public PositionAndLength(long position, int length) {
            this.position = position;
            this.length = length;
        }

    }
}
