package xyz.wagyourtail.commons.compress.virtualfs.impl.single;

import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystemFactory;
import xyz.wagyourtail.commons.compress.virtualfs.impl.SingleFileFilesystem;
import xyz.wagyourtail.commons.core.IOUtils;
import xyz.wagyourtail.commons.core.io.FastWrapOutputStream;
import xyz.wagyourtail.commons.core.io.SeekableByteChannelInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;

public class ZStandardArchiveFileSystem extends SingleFileFilesystem {
    private int compressionLevel = 3;

    public ZStandardArchiveFileSystem(VirtualFile fi) {
        super(fi);
    }

    public void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    @Override
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        try (ZstdCompressorInputStream in = new ZstdCompressorInputStream(new SeekableByteChannelInputStream(this.location.getData()))) {
            FastWrapOutputStream out = new FastWrapOutputStream();
            in.transferTo(out);
            return out.wrap();
        }
    }

    @Override
    public void write(@NotNull OutputStream os) throws IOException {
        ZstdCompressorOutputStream writer = new ZstdCompressorOutputStream(os, compressionLevel);
        Collection<VirtualFile> files = this.getFiles();
        if (files.size() != 1) {
            throw new IOException("ZStandardArchiveFile must have exactly 1 file");
        }
        SeekableByteChannel data = files.iterator().next().getData();
        data.position(0);
        IOUtils.transferTo(data, writer);
        writer.close();
    }

    public static class ZStandardArchiveFileSystemFactory extends VirtualFileSystemFactory<ZStandardArchiveFileSystem> {
        @Override
        public String[] getValidMimes() {
            return new String[]{"application/zstd"};
        }

        @Override
        public ZStandardArchiveFileSystem read(VirtualFile fi) throws IOException {
            return new ZStandardArchiveFileSystem(fi);
        }

        @Override
        public ZStandardArchiveFileSystem create(String fileName) throws IOException {
            // create empty zstd bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZstdCompressorOutputStream writer = new ZstdCompressorOutputStream(baos);
            writer.close();
            return new ZStandardArchiveFileSystem(new VirtualFile(baos.toByteArray(), fileName));
        }

    }

}
