package xyz.wagyourtail.commons.compress.virtualfs.impl.single;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
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

public class BZip2ArchiveFileSystem extends SingleFileFilesystem {

    public BZip2ArchiveFileSystem(VirtualFile fi) {
        super(fi);
    }

    @Override
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        try (SeekableByteChannel data = this.location.getData()) {
            data.position(0);
            try (BZip2CompressorInputStream in = new BZip2CompressorInputStream(new SeekableByteChannelInputStream(data))) {
                FastWrapOutputStream out = new FastWrapOutputStream();
                in.transferTo(out);
                return out.wrap();
            }
        }
    }

    @Override
    public void write(@NotNull OutputStream os) throws IOException {
        BZip2CompressorOutputStream writer = new BZip2CompressorOutputStream(os);
        Collection<VirtualFile> files = this.getFiles();
        if (files.size() != 1) {
            throw new IOException("BZip2ArchiveFile must have exactly 1 file");
        }
        SeekableByteChannel data = files.iterator().next().getData();
        data.position(0);
        SeekableByteChannelUtils.transferTo(data, writer);
        writer.close();
    }

    public static class BZip2ArchiveFileSystemFactory extends VirtualFileSystemFactory<BZip2ArchiveFileSystem> {
        @Override
        public String[] getValidMimes() {
            return new String[]{"application/x-bzip2"};
        }

        @Override
        public BZip2ArchiveFileSystem read(VirtualFile fi) throws IOException {
            return new BZip2ArchiveFileSystem(fi);
        }

        @Override
        public BZip2ArchiveFileSystem create(String fileName) throws IOException {
            // create empty bzip2 bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BZip2CompressorOutputStream writer = new BZip2CompressorOutputStream(baos);
            writer.close();
            return new BZip2ArchiveFileSystem(new VirtualFile(baos.toByteArray(), fileName));
        }
    }

}
