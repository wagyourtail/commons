package xyz.wagyourtail.commons.core.classloader.provider;

import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.commons.core.SeekableByteChannelUtils;
import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.classloader.ResourceProvider;
import xyz.wagyourtail.commons.core.function.IOSupplier;

import java.io.*;
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

public class PackedResourceProvider extends ResourceProvider {
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
            int nameLength = SeekableByteChannelUtils.readInt(channel);
            String name = SeekableByteChannelUtils.readString(channel, nameLength);
            int fileLength = SeekableByteChannelUtils.readInt(channel);
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

    public static class PositionAndLength {
        private final long position;
        private final int length;

        public PositionAndLength(long position, int length) {
            this.position = position;
            this.length = length;
        }

    }

    public static class PackedResourceOutputStream extends OutputStream {
        private final OutputStream backing;
        private final ByteArrayOutputStream temp = new ByteArrayOutputStream();

        private boolean entryOpen = false;

        public PackedResourceOutputStream(OutputStream backing) {
            this.backing = backing;
        }

        public void putNextEntry(String name) {
            if (entryOpen) closeEntry();
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            int nameLength = nameBytes.length;
            byte[] lenBytes = ByteBuffer.allocate(4).putInt(nameLength).array();
            try {
                backing.write(lenBytes);
                backing.write(nameBytes);
                entryOpen = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void closeEntry() {
            if (!entryOpen) return;
            try {
                backing.write(ByteBuffer.allocate(4).putInt(temp.size()).array());
                backing.write(temp.toByteArray());
                entryOpen = false;
                temp.reset();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void write(int b) throws IOException {
            if (entryOpen) {
                temp.write(b);
            } else {
                throw new IllegalStateException("No entry open");
            }
        }

        @Override
        public void write(@NotNull byte[] b) throws IOException {
            if (entryOpen) {
                temp.write(b);
            } else {
                throw new IllegalStateException("No entry open");
            }
        }

        @Override
        public void write(@NotNull byte[] b, int off, int len) throws IOException {
            if (entryOpen) {
                temp.write(b, off, len);
            } else {
                throw new IllegalStateException("No entry open");
            }
        }

        @Override
        public void close() throws IOException {
            closeEntry();
            backing.close();
        }
    }

}
