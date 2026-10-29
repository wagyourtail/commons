package xyz.wagyourtail.commons.compress.virtualfs.file;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.core.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

public class UrlFile extends VirtualFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlFile.class);
    private final URL url;
    private final String writeType;

    public UrlFile(URL url) {
        super(url.getFile());
        this.url = url;
        this.writeType = "PUT";
    }

    public UrlFile(URL url, String writeType) {
        super(url.getFile());
        this.url = url;
        this.writeType = writeType;
    }

    @Override
    public VirtualFile getParent() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Nullable
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
    public synchronized void setData(@Nullable SeekableByteChannel data) throws IOException {
        if (data != null) {
            try {
                URLConnection connection = this.url.openConnection();
                connection.setDoOutput(true);
                if (connection instanceof HttpURLConnection) {
                    ((HttpURLConnection) connection).setRequestMethod(this.writeType);
                }

                connection.setRequestProperty("Content-Type", getMimeType());
                connection.setRequestProperty("Content-Length", String.valueOf(data.size()));

                try (OutputStream stream = connection.getOutputStream()) {
                    IOUtils.transferTo(data, stream);
                } catch (Throwable t) {
                    LOGGER.error("Failed to set file data", t);
                }

                InputStream is = connection.getInputStream();
                if (is != null) is.close();

                val cached = CleaningSeekableByteChannel.wrap(data);
                this.cachedContents = new SoftReference<>(cached)::get;
            } catch (Throwable t) {
                LOGGER.error("Failed to set file data", t);
            }
        } else {
            URLConnection connection = this.url.openConnection();
            connection.setDoOutput(true);
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).setRequestMethod("DELETE");
            } else {
                connection.setDoOutput(true);
                connection.getOutputStream().close();
            }

            InputStream is = connection.getInputStream();
            if (is != null) is.close();
            this.cachedContents = () -> null;
        }
    }

    @Override
    public long getCompressedSize() {
        return -1;
    }

    @Override
    @SneakyThrows
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof UrlFile) {
            return this.url.toURI().equals(((UrlFile) obj).url.toURI());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.url, this.parentFs, this.path);
    }

}
