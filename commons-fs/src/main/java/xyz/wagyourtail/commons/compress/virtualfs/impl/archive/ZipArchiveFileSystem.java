package xyz.wagyourtail.commons.compress.virtualfs.impl.archive;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystemFactory;
import xyz.wagyourtail.commons.core.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;

public class ZipArchiveFileSystem extends ArchiveFileSystem<ZipArchiveEntry> {

    private final ZipFile zf;

    public ZipArchiveFileSystem(VirtualFile fi) throws IOException {
        super(fi);
        this.zf = ZipFile.builder().setIgnoreLocalFileHeader(true).setSeekableByteChannel(fi.getData()).get();
    }

    @Override
    protected Iterable<ZipArchiveEntry> getEntries() throws IOException {
        return () -> this.zf.getEntries().asIterator();
    }

    @Override
    protected InputStream getInputStream(ZipArchiveEntry entry) throws IOException {
        return this.zf.getInputStream(entry);
    }

    @Override
    @Nullable
    protected SeekableByteChannel getExtraDataIntl(VirtualFile fi) {
        ZipArchiveEntry entry = this.fileHeaders.get(fi);
        byte[] extra = entry.getExtra();
        if (extra == null) return null;
        return new SeekableInMemoryByteChannel(extra);
    }

    @Override
    public long getCompressedSize(VirtualFile fi) {
        ZipArchiveEntry entry = this.fileHeaders.get(fi);
        return entry.getCompressedSize();
    }

    @Override
    public synchronized void close() throws IOException {
        super.close();
        this.zf.close();
    }

    @Override
    public void write(OutputStream os) throws IOException {
        ZipArchiveOutputStream writer = new ZipArchiveOutputStream(os);
        for (VirtualFile entry : this.getFiles()) {
            SeekableByteChannel data = entry.getData();
            if (data == null) continue;
            writer.putArchiveEntry(new ZipArchiveEntry(entry.path));
            data.position(0);
            IOUtils.transferTo(data, writer);
            writer.closeArchiveEntry();
        }
        writer.close();
    }


    public static class ZipArchiveFileSystemFactory extends VirtualFileSystemFactory<ZipArchiveFileSystem> {
        @Override
        public String[] getValidMimes() {
            return new String[]{"application/zip", "application/java-archive", "application/vnd.android.package-archive"};
        }

        @Override
        public ZipArchiveFileSystem read(VirtualFile fi) throws IOException {
            return new ZipArchiveFileSystem(fi);
        }

        @Override
        public ZipArchiveFileSystem create(String fileName) throws IOException {
            // create empty zip bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipArchiveOutputStream writer = new ZipArchiveOutputStream(baos);
            writer.close();
            return new ZipArchiveFileSystem(new VirtualFile(baos.toByteArray(), fileName));
        }

    }

}
