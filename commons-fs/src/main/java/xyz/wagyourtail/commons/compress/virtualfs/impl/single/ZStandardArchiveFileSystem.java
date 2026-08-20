package xyz.wagyourtail.commons.compress.virtualfs.impl.single;

import lombok.Setter;
import lombok.val;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
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

public class ZStandardArchiveFileSystem extends SingleFileFilesystem {
    @Setter
    private int compressionLevel = 3;

    public ZStandardArchiveFileSystem(VirtualFile fi) {
        super(fi);
    }

    @Override
    @Nullable
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        val data = this.location.getData();
        if (data == null) return null;
        data.position(0);
        try (ZstdCompressorInputStream in = new ZstdCompressorInputStream(new SeekableByteChannelInputStream(data))) {
            FastWrapOutputStream out = new FastWrapOutputStream();
            in.transferTo(out);
            return out.wrap();
        }
    }

    @Override
    public void write(OutputStream os) throws IOException {
        try (ZstdCompressorOutputStream writer = new ZstdCompressorOutputStream(os, compressionLevel)) {
            Collection<VirtualFile> files = this.getFiles();
            if (files.size() != 1) {
                throw new IOException("ZStandardArchiveFile must have exactly 1 file");
            }
            SeekableByteChannel data = files.iterator().next().getData();
            if (data == null) return;
            data.position(0);
            IOUtils.transferTo(data, writer);
        }
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
