package xyz.wagyourtail.commons.compress.virtualfs.impl.archive;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZipArchiveFileSystem extends VirtualFileSystem {

    private final ZipFile zf;
    private final Map<VirtualFile, ZipArchiveEntry> fileHeaders = new ConcurrentHashMap<>();

    public ZipArchiveFileSystem(VirtualFile fi) throws IOException {
        super(fi);
        this.zf = ZipFile.builder().setIgnoreLocalFileHeader(true).setSeekableByteChannel(fi.getData()).get();
    }

    public void putExisting(Map<String, VirtualFile> files, String path, ZipArchiveEntry header) {
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

    protected Map<String, VirtualFile> resolveFiles() {
        Map<String, VirtualFile> files = new HashMap<>();
        Iterator<ZipArchiveEntry> iter = this.zf.getEntries().asIterator();
        while (iter.hasNext()) {
            ZipArchiveEntry entry = iter.next();
            String path = entry.getName();
            this.putExisting(files, path, entry);
        }
        return files;
    }

    @Override
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        ZipArchiveEntry entry = this.fileHeaders.get(fi);
        FastWrapOutputStream out = new FastWrapOutputStream();
        this.zf.getInputStream(entry).transferTo(out);
        return out.wrap();
    }

    @Override
    protected SeekableByteChannel getExtraDataIntl(VirtualFile fi) {
        ZipArchiveEntry entry = this.fileHeaders.get(fi);
        byte[] extra = entry.getExtra();
        if (extra == null) return null;
        return new SeekableInMemoryByteChannel(extra);
    }

    @Override
    public long getSize(VirtualFile fi) throws IOException {
        ZipArchiveEntry entry = this.fileHeaders.get(fi);
        return entry.getSize();
    }

    @Override
    public long getCompressedSize(VirtualFile fi) {
        ZipArchiveEntry entry = this.fileHeaders.get(fi);
        return entry.getCompressedSize();
    }

    @Override
    public synchronized void close() throws Exception {
        super.close();
        this.zf.close();
    }

    @Override
    public void write(@NotNull OutputStream os) throws IOException {
        ZipArchiveOutputStream writer = new ZipArchiveOutputStream(os);
        for (VirtualFile entry : this.getFiles()) {
            writer.putArchiveEntry(new ZipArchiveEntry(entry.path));
            SeekableByteChannel data = entry.getData();
            data.position(0);
            SeekableByteChannelUtils.transferTo(data, writer);
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
