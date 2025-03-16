package xyz.wagyourtail.commons.compress.virtualfs.file;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

public class UrlFile extends VirtualFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlFile.class);
    private final URL url;

    public UrlFile(URL url) {
        super((VirtualFileSystem) null, url.getFile());
        this.url = url;
    }

    @Override
    public VirtualFile getParent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel getData() throws IOException {
        CleaningSeekableByteChannel cached = this.cachedContents.get();
        if (cached == null) {
            synchronized (this) {
                cached = this.cachedContents.get();
                if (cached == null) {
                    try (InputStream stream = this.url.openStream()) {
                        byte[] data = stream.readAllBytes();
                        cached = CleaningSeekableByteChannel.wrap(new SeekableInMemoryByteChannel(data));
                        this.cachedContents = new SoftReference<>(cached)::get;
                    } catch (Throwable t) {
                        LOGGER.error("Failed to get file data", t);
                        return null;
                    }
                }
            }
        }
        return cached;
    }

    @Override
    public long getCompressedSize() {
        return -1;
    }

    @Override
    public synchronized void setData(SeekableByteChannel data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UrlFile) {
            return this.url.equals(((UrlFile) obj).url);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.url, this.parentFs, this.path);
    }

}
