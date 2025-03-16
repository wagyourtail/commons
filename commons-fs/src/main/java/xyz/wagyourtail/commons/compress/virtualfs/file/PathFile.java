package xyz.wagyourtail.commons.compress.virtualfs.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystem;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystemFactory;
import xyz.wagyourtail.commons.core.data.SeekableByteChannelInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class PathFile extends VirtualFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathFile.class);
    private final Path path;

    public PathFile(Path path) {
        super((VirtualFileSystem) null, path.toAbsolutePath().toString());
        this.path = path;
    }

    public Path getPath() {
        return this.path;
    }

    @Override
    public boolean isArchiveFile() throws IOException {
        return isArchiveFile(this.path);
    }

    @Override
    public boolean isClassFile() throws IOException {
        return isClassFile(this.path);
    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(this.path);
    }

    @Override
    public boolean isFile() throws IOException {
        return !this.isDirectory();
    }

    @Override
    public VirtualFile getParent() throws IOException {
        Path parent = this.path.getParent();
        if (parent == null) return null;
        return new PathFile(parent);
    }

    @Override
    public String fileName() {
        return this.path.getFileName().toString();
    }

    @Override
    public SeekableByteChannel getData() throws IOException {
        if (!Files.exists(this.path)) return null;
        CleaningSeekableByteChannel cached = this.cachedContents.get();
        if (cached == null) {
            synchronized (this) {
                cached = this.cachedContents.get();
                if (cached == null) {
                    cached = CleaningSeekableByteChannel.wrap(Files.newByteChannel(this.path));
                    this.cachedContents = new SoftReference<>(cached)::get;
                }
            }
        }
        return cached.position(0);
    }

    @Override
    public long getCompressedSize() throws IOException {
        return Files.size(this.path);
    }

    @Override
    public synchronized void setData(SeekableByteChannel data) throws IOException {
        CleaningSeekableByteChannel cached = CleaningSeekableByteChannel.wrap(data);
        this.cachedContents = new SoftReference<>(cached)::get;
        Files.copy(new SeekableByteChannelInputStream(data), this.path, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public String getMimeType() throws IOException {
        if (!this.isFile()) {
            return "inode/directory";
        } else {
            if (this.mimeType == null) {
                synchronized (this) {
                    if (this.mimeType == null) {
                        return this.mimeType = getMimeType(this.path);
                    }
                }
            }
            return this.mimeType;
        }
    }

    @Override
    public String toString() {
        return "PathFileInfo: " + this.path;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PathFile && this.path.equals(((PathFile) obj).path);
    }

    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

    public static boolean isClassFile(Path path) throws IOException {
        if (!path.toString().endsWith(".class") && !path.toString().endsWith(".class/")) return false;
        try (InputStream t = Files.newInputStream(path)) {
            byte[] arr = new byte[4];
            if (t.read(arr, 0, 4) != 4) return false;
            return Arrays.equals(arr, CLASS_MAGIC);
        }
    }

    public static String getMimeType(Path name) throws IOException {
        // short circuit for class files, because they are the most common for our use case
        if (isClassFile(name)) {
            return "application/java-vm";
        }
        return tika.detect(name);
    }

    public static boolean isArchiveFile(Path path) throws IOException {
        return VirtualFileSystemFactory.getSupportedMimes().contains(getMimeType(path));
    }

}
