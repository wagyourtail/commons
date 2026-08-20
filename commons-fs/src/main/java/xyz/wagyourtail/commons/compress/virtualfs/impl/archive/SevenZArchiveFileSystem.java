package xyz.wagyourtail.commons.compress.virtualfs.impl.archive;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystemFactory;
import xyz.wagyourtail.commons.core.IOUtils;
import xyz.wagyourtail.commons.core.io.SeekableByteChannelInputStream;
import xyz.wagyourtail.commons.core.io.SeekableInMemoryByteChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;

public class SevenZArchiveFileSystem extends ArchiveFileSystem<SevenZArchiveEntry> {

    SevenZFile sevenZ;

    public SevenZArchiveFileSystem(VirtualFile fi) throws IOException {
        super(fi);
        this.sevenZ = SevenZFile.builder().setTryToRecoverBrokenArchives(true).setUseDefaultNameForUnnamedEntries(true).setSeekableByteChannel(fi.getData()).get();
    }

    @Override
    protected Iterable<SevenZArchiveEntry> getEntries() throws IOException {
        return this.sevenZ.getEntries();
    }

    @Override
    protected InputStream getInputStream(SevenZArchiveEntry entry) throws IOException {
        return this.sevenZ.getInputStream(entry);
    }

    @Override
    public void write(OutputStream os) throws IOException {
        SeekableInMemoryByteChannel buffer = new SeekableInMemoryByteChannel();
        try (SevenZOutputFile writer = new SevenZOutputFile(buffer)) {
            for (VirtualFile entry : this.getFiles()) {
                SeekableByteChannel data = entry.getData();
                if (data == null) continue;
                SevenZArchiveEntry header = new SevenZArchiveEntry();
                header.setName(entry.path);
                writer.putArchiveEntry(header);
                data.position(0);
                writer.write(new SeekableByteChannelInputStream(data));
                writer.closeArchiveEntry();
            }
        }
        buffer.position(0);
        IOUtils.transferTo(buffer, os);
    }

    @Override
    public synchronized void close() throws IOException {
        super.close();
        this.sevenZ.close();
    }

    public static class SevenZArchiveFileSystemFactory extends VirtualFileSystemFactory<SevenZArchiveFileSystem> {

        public String[] getValidMimes() {
            return new String[]{"application/x-7z-compressed"};
        }

        public SevenZArchiveFileSystem read(VirtualFile fi) throws IOException {
            return new SevenZArchiveFileSystem(fi);
        }

        public SevenZArchiveFileSystem create(String fileName) throws IOException {
            SeekableInMemoryByteChannel baos = new SeekableInMemoryByteChannel();
            new SevenZOutputFile(baos).close();
            baos.hardTruncate(baos.size());
            return new SevenZArchiveFileSystem(new VirtualFile(baos, fileName));
        }

    }

}
