package xyz.wagyourtail.commons.compress.virtualfs.impl.archive;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystemFactory;
import xyz.wagyourtail.commons.core.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;

public class TarArchiveFileSystem extends ArchiveFileSystem<TarArchiveEntry> {

    private final TarFile tar;

    public TarArchiveFileSystem(VirtualFile fi) throws IOException {
        super(fi);
        this.tar = new TarFile(fi.getData());
    }

    @Override
    protected Iterable<TarArchiveEntry> getEntries() throws IOException {
        return this.tar.getEntries();
    }

    @Override
    protected InputStream getInputStream(TarArchiveEntry entry) throws IOException {
        return this.tar.getInputStream(entry);
    }

    @Override
    public void write(OutputStream os) throws IOException {
        TarArchiveOutputStream writer = new TarArchiveOutputStream(os);
        for (VirtualFile entry : this.getFiles()) {
            SeekableByteChannel data = entry.getData();
            if (data == null) continue;
            writer.putArchiveEntry(new TarArchiveEntry(entry.path));
            data.position(0);
            IOUtils.transferTo(data, writer);
            writer.closeArchiveEntry();
        }
        writer.close();
    }

    @Override
    public synchronized void close() throws IOException {
        super.close();
        this.tar.close();
    }

    public static class TarArchiveFileSystemFactory extends VirtualFileSystemFactory<TarArchiveFileSystem> {
        @Override
        public String[] getValidMimes() {
            return new String[]{"application/x-tar", "application/x-gtar"};
        }

        @Override
        public TarArchiveFileSystem read(VirtualFile fi) throws IOException {
            return new TarArchiveFileSystem(fi);
        }

        @Override
        public TarArchiveFileSystem create(String fileName) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TarArchiveOutputStream writer = new TarArchiveOutputStream(baos);
            writer.close();
            return new TarArchiveFileSystem(new VirtualFile(baos.toByteArray(), fileName));
        }

    }

}
