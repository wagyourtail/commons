package xyz.wagyourtail.commons.compress.virtualfs.impl.single;

import lombok.val;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorInputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorOutputStream;
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

public class Pack200ArchiveFileSystem extends SingleFileFilesystem {

    public Pack200ArchiveFileSystem(VirtualFile fi) {
        super(fi);
    }

    @Override
    @Nullable
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        val data = this.location.getData();
        if (data == null) return null;
        data.position(0);
        try (Pack200CompressorInputStream in = new Pack200CompressorInputStream(new SeekableByteChannelInputStream(data))) {
            FastWrapOutputStream out = new FastWrapOutputStream();
            in.transferTo(out);
            return out.wrap();
        }
    }

    @Override
    public void write(OutputStream os) throws IOException {
        try (Pack200CompressorOutputStream writer = new Pack200CompressorOutputStream(os)) {
            Collection<VirtualFile> files = this.getFiles();
            if (files.size() != 1) {
                throw new IOException("Pack200ArchiveFile must have exactly 1 file");
            }
            SeekableByteChannel data = files.iterator().next().getData();
            if (data == null) return;
            data.position(0);
            IOUtils.transferTo(data, writer);
        }
    }


    public static class Pack200ArchiveFactory extends VirtualFileSystemFactory<Pack200ArchiveFileSystem> {
        @Override
        public String[] getValidMimes() {
            return new String[]{"application/x-java-pack200"};
        }

        @Override
        public Pack200ArchiveFileSystem read(VirtualFile fi) throws IOException {
            return new Pack200ArchiveFileSystem(fi);
        }

        @Override
        public Pack200ArchiveFileSystem create(String fileName) throws IOException {
            // create empty pack200 bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Pack200CompressorOutputStream writer = new Pack200CompressorOutputStream(baos);
            writer.close();
            return new Pack200ArchiveFileSystem(new VirtualFile(baos.toByteArray(), fileName));
        }

    }

}
