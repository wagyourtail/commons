package xyz.wagyourtail.commons.compress.virtualfs.impl.single;

import lombok.val;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystemFactory;
import xyz.wagyourtail.commons.core.IOUtils;
import xyz.wagyourtail.commons.core.io.FastWrapOutputStream;
import xyz.wagyourtail.commons.core.io.SeekableByteChannelInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;

public class GZipArchiveFileSystem extends SingleFileFilesystem {

    public GZipArchiveFileSystem(VirtualFile fi) {
        super(fi);
    }

    @Override
    @Nullable
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        val data = this.location.getData();
        if (data == null) return null;
        data.position(0);
        try (GzipCompressorInputStream in = new GzipCompressorInputStream(new SeekableByteChannelInputStream(data))) {
            FastWrapOutputStream out = new FastWrapOutputStream();
            in.transferTo(out);
            return out.wrap();
        }
    }

    @Override
    public void write(OutputStream os) throws IOException {
        try (GzipCompressorOutputStream writer = new GzipCompressorOutputStream(os)) {
            Collection<VirtualFile> files = this.getFiles();
            if (files.size() != 1) {
                throw new IOException("GZipArchiveFile must have exactly 1 file");
            }
            SeekableByteChannel data = files.iterator().next().getData();
            if (data == null) return;
            data.position(0);
            IOUtils.transferTo(data, writer);
        }
    }


    public static class GZipArchiveFileSystemFactory extends VirtualFileSystemFactory<GZipArchiveFileSystem> {
        @Override
        public String[] getValidMimes() {
            return new String[]{"application/gzip"};
        }

        @Override
        public GZipArchiveFileSystem read(VirtualFile fi) throws IOException {
            return new GZipArchiveFileSystem(fi);
        }

        @Override
        public GZipArchiveFileSystem create(String fileName) throws IOException {
            // create empty gzip bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GzipCompressorOutputStream writer = new GzipCompressorOutputStream(baos);
            writer.close();
            return new GZipArchiveFileSystem(new VirtualFile(baos.toByteArray(), fileName));
        }

    }

}
