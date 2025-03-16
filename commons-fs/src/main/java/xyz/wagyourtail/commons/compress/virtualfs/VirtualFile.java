package xyz.wagyourtail.commons.compress.virtualfs;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.function.IOSupplier;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.wagyourtail.commons.core.data.SeekableByteChannelInputStream;

import javax.swing.*;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;
import java.util.function.Supplier;

public class VirtualFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualFile.class);
    protected static final byte[] CLASS_MAGIC = new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};
    protected static final Tika tika = new Tika();
    private static final Cleaner CLEANER = Cleaner.create();

    public final String path;
    public final VirtualFileSystem parentFs;
    protected volatile Supplier<CleaningSeekableByteChannel> cachedContents = () -> null;
    protected volatile String mimeType;
    public Icon icon;

    public VirtualFile(final byte[] data, final String path) {
        this(new SeekableInMemoryByteChannel(data), path);
    }

    public VirtualFile(final SeekableByteChannel hardRef, final String path) {
        CleaningSeekableByteChannel data = CleaningSeekableByteChannel.wrap(hardRef);
        // stores to field on lambda, so it doesn't get garbage collected
        this.cachedContents = () -> data;
        this.path = path;
        this.parentFs = null;
    }

    public VirtualFile(VirtualFileSystem archive, String path) {
        this.parentFs = archive;
        this.path = path;
    }

    public static boolean isArchiveFile(String path, IOSupplier<SeekableByteChannel> bytes) throws IOException {
        return VirtualFileSystemFactory.getSupportedMimes().contains(getMimeType(path, bytes));
    }

    public static boolean isClassFile(String path, IOSupplier<SeekableByteChannel> bytes) throws IOException {
        if (!path.endsWith(".class") && !path.endsWith(".class/")) return false;
        SeekableByteChannel buf = bytes.get();
        if (buf == null) return false;
        if (buf.size() < 4) return false;
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buf.position(0);
        buf.read(buffer);
        buffer.flip();
        return buffer.getInt() == 0xCAFEBABE;
    }

    public static String getMimeType(String name, IOSupplier<SeekableByteChannel> bytes) throws IOException {
        // short circuit for class files, because they are the most common for our use case
        if (isClassFile(name, bytes)) {
            return "application/java-vm";
        }
        SeekableByteChannel data = bytes.get();
        data.position(0);
        try (SeekableByteChannelInputStream t = new SeekableByteChannelInputStream(data)) {
            return tika.detect(t, name);
        } catch (Throwable t) {
            LOGGER.error("Failed to get mime type for {}", name, t);
            return "application/octet-stream";
        }
    }

    public boolean isDirectory() {
        return this.path.endsWith("/");
    }

    public boolean isFile() throws IOException {
        if (!this.isDirectory()) return true;
        if (this.getData() == null) return false;
        return this.getData().size() > 0L;
    }

    public VirtualFile getParent() throws IOException {
        int slash;
        if (this.path.endsWith("/")) {
            slash = this.path.substring(0, this.path.length() - 1).lastIndexOf('/');
        } else {
            slash = this.path.lastIndexOf('/');
        }
        if (slash == -1) {
            if (this.path.isEmpty()) return null;
            return this.parentFs.getFile("");
        }
        VirtualFile fi = this.parentFs.getFile(this.path.substring(0, slash + 1));
        if (fi == null) {
            LOGGER.warn("Parent file not found: {}", this.path.substring(0, slash + 1));
            return new VirtualFile(this.parentFs, this.path.substring(0, slash + 1));
        }
        return fi;
    }

    public String fileName() {
        int slash;
        if (this.path.endsWith("/")) {
            slash = this.path.substring(0, this.path.length() - 1).lastIndexOf('/');
        } else {
            slash = this.path.lastIndexOf('/');
        }
        if (slash == -1) return this.path;
        return this.path.substring(slash + 1);
    }

    public String normalizedFileName() {
        int slash;
        if (this.path.endsWith("/")) {
            slash = this.path.substring(0, this.path.length() - 1).lastIndexOf('/');
            if (slash == -1) return this.path.substring(0, this.path.length() - 1);
            return this.path.substring(slash + 1, this.path.length() - 1);
        } else {
            slash = this.path.lastIndexOf('/');
            if (slash == -1) return this.path;
            return this.path.substring(slash + 1);
        }
    }

    public String nameWithoutExtension() {
        String name = this.normalizedFileName();
        if (name.contains(".")) name = name.substring(0, name.lastIndexOf('.'));
        return name;
    }

    public String className() {
        String name = this.path;
        if (name.contains(".")) name = name.substring(0, name.lastIndexOf('.'));
        return name;
    }

    public SeekableByteChannel getData() throws IOException {
        CleaningSeekableByteChannel cached = this.cachedContents.get();
        if (cached == null) {
            synchronized (this) {
                cached = this.cachedContents.get();
                if (cached == null) {
                    try {
                        SeekableByteChannel uncompressed = this.parentFs.getData(this);
                        cached = CleaningSeekableByteChannel.wrap(uncompressed);
                        this.cachedContents = new SoftReference<>(cached)::get;
                    } catch (Throwable t) {
                        LOGGER.error("Failed to get file data for {}", this.path, t);
                        return null;
                    }
                }
            }
        }
        return cached;
    }

    public SeekableByteChannel getExtraData() {
        if (this.parentFs == null) return null;
        try {
            return this.parentFs.getExtraData(this);
        } catch (Throwable t) {
            LOGGER.error("Failed to get file extra data for {}", this.path, t);
            return null;
        }
    }

    public byte[] getBytes() throws IOException {
        SeekableByteChannel data = this.getData();
        if (data == null) throw new NullPointerException("SeekableByteChannel of file " + this.path + " is null");
        data.position(0);
        return new SeekableByteChannelInputStream(data).readAllBytes();
    }

    public long getCompressedSize() throws IOException {
        return this.parentFs.getCompressedSize(this);
    }

    public long getSize() throws IOException {
        return this.parentFs.getSize(this);
    }

    public synchronized void setData(SeekableByteChannel data) throws IOException {
        CleaningSeekableByteChannel cached = CleaningSeekableByteChannel.wrap(data);
        this.cachedContents = () -> cached;
        this.parentFs.putData(this, cached);
    }

    public boolean isArchiveFile() throws IOException {
        return isArchiveFile(this.path, this::getData);
    }

    public boolean isClassFile() throws IOException {
        return isClassFile(this.path, this::getData);
    }

    public String getMimeType() throws IOException {
        if (!this.isFile()) {
            return "inode/directory";
        } else {
            if (this.mimeType == null) {
                synchronized (this) {
                    if (this.mimeType == null) {
                        return this.mimeType = getMimeType(this.normalizedFileName(), this::getData);
                    }
                }
            }
            return this.mimeType;
        }
    }

    @Override
    public String toString() {
        return "FileInfo[path=" + this.path + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VirtualFile)) return false;
        return this.parentFs == ((VirtualFile) obj).parentFs && this.path.equals(((VirtualFile) obj).path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parentFs, this.path);
    }

    protected static class CleaningSeekableByteChannel implements SeekableByteChannel {

        private final SeekableByteChannel data;

        private CleaningSeekableByteChannel(SeekableByteChannel data) {
            this.data = data;
            CLEANER.register(this, () -> {
                try {
                    data.close();
                } catch (Throwable e) {
                    LOGGER.error("Failed to close file data", e);
                }
            });
        }

        public static CleaningSeekableByteChannel wrap(SeekableByteChannel data) {
            if (data == null) return null;
            if (data instanceof CleaningSeekableByteChannel) return (CleaningSeekableByteChannel) data;
            return new CleaningSeekableByteChannel(data);
        }

        public SeekableByteChannel get() {
            return this.data;
        }

        @Override
        public boolean isOpen() {
            return this.data.isOpen();
        }

        @Override
        public void close() throws IOException {
            this.data.close();
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            return this.data.read(dst);
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            return this.data.write(src);
        }

        @Override
        public long position() throws IOException {
            return this.data.position();
        }

        @Override
        public SeekableByteChannel position(long newPosition) throws IOException {
            this.data.position(newPosition);
            return this;
        }

        @Override
        public long size() throws IOException {
            return this.data.size();
        }

        @Override
        public SeekableByteChannel truncate(long size) throws IOException {
            this.data.truncate(size);
            return this;
        }
    }

}
