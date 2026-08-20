package xyz.wagyourtail.commons.compress.virtualfs.impl.single;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
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

public class BZip2ArchiveFileSystem extends SingleFileFilesystem {

    public BZip2ArchiveFileSystem(VirtualFile fi) {
        super(fi);
    }

    @Override
    @Nullable
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        try (SeekableByteChannel data = this.location.getData()) {
            if (data == null) return null;
            data.position(0);
            try (BZip2CompressorInputStream in = new BZip2CompressorInputStream(new SeekableByteChannelInputStream(data))) {
                FastWrapOutputStream out = new FastWrapOutputStream();
                in.transferTo(out);
                return out.wrap();
            }
        }
    }

    @Override
    public void write(OutputStream os) throws IOException {
        try (BZip2CompressorOutputStream writer = new BZip2CompressorOutputStream(os)) {
            Collection<VirtualFile> files = this.getFiles();
            if (files.size() != 1) {
                throw new IOException("BZip2ArchiveFile must have exactly 1 file");
            }
            SeekableByteChannel data = files.iterator().next().getData();
            if (data == null) return;
            data.position(0);
            IOUtils.transferTo(data, writer);
        }
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
