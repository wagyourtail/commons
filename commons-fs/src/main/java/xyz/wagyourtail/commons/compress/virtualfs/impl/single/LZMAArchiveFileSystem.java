package xyz.wagyourtail.commons.compress.virtualfs.impl.single;

import lombok.val;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
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

public class LZMAArchiveFileSystem extends SingleFileFilesystem {

    public LZMAArchiveFileSystem(VirtualFile fi) {
        super(fi);
    }

    @Override
    @Nullable
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        val data = this.location.getData();
        if (data == null) return null;
        data.position(0);
        try (LZMACompressorInputStream in = new LZMACompressorInputStream(new SeekableByteChannelInputStream(data))) {
            FastWrapOutputStream out = new FastWrapOutputStream();
            in.transferTo(out);
            return out.wrap();
        }
    }

    @Override
    public void write(OutputStream os) throws IOException {
        try (LZMACompressorOutputStream writer = new LZMACompressorOutputStream(os)) {
            Collection<VirtualFile> files = this.getFiles();
            if (files.size() != 1) {
                throw new IOException("LZMAArchiveFile must have exactly 1 file");
            }
            SeekableByteChannel data = files.iterator().next().getData();
            if (data == null) return;
            data.position(0);
            IOUtils.transferTo(data, writer);
        }
    }


    public static class LZMAArchiveFileSystemFactory extends VirtualFileSystemFactory<LZMAArchiveFileSystem> {
        @Override
        public String[] getValidMimes() {
            return new String[]{"application/x-lzma"};
        }

        @Override
        public LZMAArchiveFileSystem read(VirtualFile fi) throws IOException {
            return new LZMAArchiveFileSystem(fi);
        }

        @Override
        public LZMAArchiveFileSystem create(String fileName) throws IOException {
            // create empty lzma bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            LZMACompressorOutputStream writer = new LZMACompressorOutputStream(baos);
            writer.close();
            return new LZMAArchiveFileSystem(new VirtualFile(baos.toByteArray(), fileName));
        }

    }

}
