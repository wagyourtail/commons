package xyz.wagyourtail.commons.compress.virtualfs.impl.single;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystemFactory;
import xyz.wagyourtail.commons.compress.virtualfs.impl.SingleFileFilesystem;
import xyz.wagyourtail.commons.core.data.FastWrapOutputStream;
import xyz.wagyourtail.commons.core.data.SeekableByteChannelInputStream;
import xyz.wagyourtail.commons.core.data.SeekableByteChannelUtils;

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
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        try (GzipCompressorInputStream in = new GzipCompressorInputStream(new SeekableByteChannelInputStream(this.location.getData()))) {
            FastWrapOutputStream out = new FastWrapOutputStream();
            in.transferTo(out);
            return out.wrap();
        }
    }

    @Override
    public void write(@NotNull OutputStream os) throws IOException {
        GzipCompressorOutputStream writer = new GzipCompressorOutputStream(os);
        Collection<VirtualFile> files = this.getFiles();
        if (files.size() != 1) {
            throw new IOException("GZipArchiveFile must have exactly 1 file");
        }
        SeekableByteChannel data = files.iterator().next().getData();
        data.position(0);
        SeekableByteChannelUtils.transferTo(data, writer);
        writer.close();
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
