package xyz.wagyourtail.commons.compress.virtualfs.impl.single;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.jetbrains.annotations.NotNull;
import org.tukaani.xz.XZ;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystemFactory;
import xyz.wagyourtail.commons.compress.virtualfs.impl.SingleFileFilesystem;
import xyz.wagyourtail.commons.core.data.FastWrapOutputStream;
import xyz.wagyourtail.commons.core.data.SeekableByteChannelInputStream;
import xyz.wagyourtail.commons.core.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;

public class XZArchiveFileSystem extends SingleFileFilesystem {
    private int compressionLevel = 6;

    public XZArchiveFileSystem(VirtualFile fi) {
        super(fi);
    }

    public void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    @Override
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        try (XZCompressorInputStream in = new XZCompressorInputStream(new SeekableByteChannelInputStream(this.location.getData()))) {
            FastWrapOutputStream out = new FastWrapOutputStream();
            in.transferTo(out);
            return out.wrap();
        }
    }

    @Override
    public void write(@NotNull OutputStream os) throws IOException {
        XZCompressorOutputStream writer = new XZCompressorOutputStream(os, compressionLevel);
        Collection<VirtualFile> files = this.getFiles();
        if (files.size() != 1) {
            throw new IOException("XZArchiveFile must have exactly 1 file");
        }
        SeekableByteChannel data = files.iterator().next().getData();
        data.position(0);
        IOUtils.transferTo(data, writer);
        writer.close();
    }


    public static class XZArchiveFileSystemFactory extends VirtualFileSystemFactory<XZArchiveFileSystem> {
        @Override
        public String[] getValidMimes() {
            return new String[]{"application/x-xz"};
        }

        @Override
        public XZArchiveFileSystem read(VirtualFile fi) throws IOException {
            return new XZArchiveFileSystem(fi);
        }

        @Override
        public XZArchiveFileSystem create(String fileName) throws IOException {
            // create empty xz bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XZCompressorOutputStream writer = new XZCompressorOutputStream(baos, XZ.CHECK_CRC64);
            writer.close();
            return new XZArchiveFileSystem(new VirtualFile(baos.toByteArray(), fileName));
        }

    }

}
