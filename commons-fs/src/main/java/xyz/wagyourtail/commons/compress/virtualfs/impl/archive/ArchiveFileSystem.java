package xyz.wagyourtail.commons.compress.virtualfs.impl.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystem;
import xyz.wagyourtail.commons.core.io.FastWrapOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.HashMap;
import java.util.Map;

public abstract class ArchiveFileSystem<T extends ArchiveEntry> extends VirtualFileSystem {

    protected final Map<VirtualFile, T> fileHeaders = new HashMap<>();

    public ArchiveFileSystem(VirtualFile location) {
        super(location);
    }

    public void putExisting(Map<String, VirtualFile> files, String path, T header) {
        VirtualFile fi = files.get(path);
        if (fi != null) {
            // rename existing file
            boolean isDir = path.endsWith("/");
            if (isDir) {
                path = path.substring(0, path.length() - 1);
            }
            int i = 1;
            while (files.containsKey(path + " (duplicate " + i + ")" + (isDir ? "/" : ""))) {
                i++;
            }
            String newPath = path + " (duplicate " + i + ")" + (isDir ? "/" : "");
            fi = new VirtualFile(this, newPath);
            this.fileHeaders.put(fi, this.fileHeaders.get(files.get(path)));
            files.put(newPath, fi);
        }
        fi = new VirtualFile(this, path);
        this.fileHeaders.put(fi, header);
        files.put(path, fi);
    }

    @Override
    protected Map<String, VirtualFile> resolveFiles() throws IOException {
        Map<String, VirtualFile> files = new HashMap<>();
        for (T entry : this.getEntries()) {
            String path = entry.getName();
            this.putExisting(files, path, entry);
        }
        return files;
    }

    @Override
    protected @Nullable SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        T entry = this.fileHeaders.get(fi);
        long size = entry.getSize();
        if (size > Integer.MAX_VALUE) {
            throw new IOException("File too large");
        }
        FastWrapOutputStream out = new FastWrapOutputStream();
        this.getInputStream(entry).transferTo(out);
        return out.wrap();
    }

    protected abstract Iterable<T> getEntries() throws IOException;

    protected abstract InputStream getInputStream(T entry) throws IOException;

    @Override
    public long getSize(VirtualFile fi) throws IOException {
        T entry = this.fileHeaders.get(fi);
        return entry.getSize();
    }

    @Override
    public long getCompressedSize(VirtualFile fi) throws IOException {
        return -1;
    }

}
