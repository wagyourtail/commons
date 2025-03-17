package xyz.wagyourtail.commons.compress.virtualfs.impl.archive;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystem;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystemFactory;
import xyz.wagyourtail.commons.core.data.FastWrapOutputStream;
import xyz.wagyourtail.commons.core.data.SeekableByteChannelUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.HashMap;
import java.util.Map;

public class TarArchiveFileSystem extends VirtualFileSystem {

    private final TarFile tar;
    private final Map<VirtualFile, TarArchiveEntry> fileHeaders = new HashMap<>();

    public TarArchiveFileSystem(VirtualFile fi) throws IOException {
        super(fi);
        this.tar = new TarFile(fi.getData());
    }

    public void putExisting(Map<String, VirtualFile> files, String path, TarArchiveEntry header) {
        VirtualFile fi = files.get(path);
        if (fi != null) {
            // rename existing file
            int i = 1;
            while (files.containsKey(path + " (duplicate " + i + ")")) {
                i++;
            }
            String newPath = path + " (duplicate " + i + ")";
            fi = new VirtualFile(this, newPath);
            this.fileHeaders.put(fi, this.fileHeaders.get(files.get(path)));
            files.put(newPath, fi);
        }
        fi = new VirtualFile(this, path);
        this.fileHeaders.put(fi, header);
        files.put(path, fi);
    }

    @Override
    protected Map<String, VirtualFile> resolveFiles() {
        Map<String, VirtualFile> files = new HashMap<>();
        for (TarArchiveEntry entry : this.tar.getEntries()) {
            String path = entry.getName();
            this.putExisting(files, path, entry);
        }
        return files;
    }

    @Override
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        TarArchiveEntry entry = this.fileHeaders.get(fi);
        long size = entry.getRealSize();
        if (size > Integer.MAX_VALUE) {
            throw new IOException("File too large");
        }
        FastWrapOutputStream out = new FastWrapOutputStream();
        this.tar.getInputStream(entry).transferTo(out);
        return out.wrap();
    }

    @Override
    public long getSize(VirtualFile fi) {
        TarArchiveEntry entry = this.fileHeaders.get(fi);
        return entry.getRealSize();
    }

    @Override
    public long getCompressedSize(VirtualFile fi) {
        TarArchiveEntry entry = this.fileHeaders.get(fi);
        return entry.getRealSize();
    }

    @Override
    public void write(@NotNull OutputStream os) throws IOException {
        TarArchiveOutputStream writer = new TarArchiveOutputStream(os);
        for (VirtualFile entry : this.getFiles()) {
            writer.putArchiveEntry(new TarArchiveEntry(entry.path));
            SeekableByteChannel data = entry.getData();
            data.position(0);
            SeekableByteChannelUtils.transferTo(data, writer);
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
            // create empty tar bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TarArchiveOutputStream writer = new TarArchiveOutputStream(baos);
            writer.close();
            return new TarArchiveFileSystem(new VirtualFile(baos.toByteArray(), fileName));
        }

    }

}
