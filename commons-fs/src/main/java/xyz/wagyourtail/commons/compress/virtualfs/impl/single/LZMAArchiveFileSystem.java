package xyz.wagyourtail.commons.compress.virtualfs.impl.single;

import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
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

public class LZMAArchiveFileSystem extends SingleFileFilesystem {

    public LZMAArchiveFileSystem(VirtualFile fi) {
        super(fi);
    }

    @Override
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        try (LZMACompressorInputStream in = new LZMACompressorInputStream(new SeekableByteChannelInputStream(this.location.getData()))) {
            FastWrapOutputStream out = new FastWrapOutputStream();
            in.transferTo(out);
            return out.wrap();
        }
    }

    @Override
    public void write(@NotNull OutputStream os) throws IOException {
        LZMACompressorOutputStream writer = new LZMACompressorOutputStream(os);
        Collection<VirtualFile> files = this.getFiles();
        if (files.size() != 1) {
            throw new IOException("LZMAArchiveFile must have exactly 1 file");
        }
        SeekableByteChannel data = files.iterator().next().getData();
        data.position(0);
        IOUtils.transferTo(data, writer);
        writer.close();
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
