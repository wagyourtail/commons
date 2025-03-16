package xyz.wagyourtail.commons.compress.virtualfs.impl.archive;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystem;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystemFactory;
import xyz.wagyourtail.commons.core.data.FastWrapOutputStream;
import xyz.wagyourtail.commons.core.data.SeekableByteChannelInputStream;
import xyz.wagyourtail.commons.core.data.SeekableByteChannelUtils;
import xyz.wagyourtail.commons.core.data.SeekableInMemoryByteChannel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.HashMap;
import java.util.Map;

public class SevenZArchiveFileSystem extends VirtualFileSystem {

    SevenZFile sevenZ;
    private final Map<VirtualFile, SevenZArchiveEntry> fileHeaders = new HashMap<>();

    public SevenZArchiveFileSystem(VirtualFile fi) throws IOException {
        super(fi);
        this.sevenZ = SevenZFile.builder().setTryToRecoverBrokenArchives(true).setUseDefaultNameForUnnamedEntries(true).setSeekableByteChannel(fi.getData()).get();
    }

    public void putExisting(Map<String, VirtualFile> files, String path, SevenZArchiveEntry header) {
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
        for (SevenZArchiveEntry entry : this.sevenZ.getEntries()) {
            String path = entry.getName();
            this.putExisting(files, path, entry);
        }
        return files;
    }


    @Override
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        SevenZArchiveEntry entry = this.fileHeaders.get(fi);
        long size = entry.getSize();
        if (size > Integer.MAX_VALUE) {
            throw new IOException("File too large");
        }
        FastWrapOutputStream out = new FastWrapOutputStream();
        this.sevenZ.getInputStream(entry).transferTo(out);
        return out.wrap();
    }

    @Override
    public long getSize(VirtualFile fi) throws IOException {
        SevenZArchiveEntry entry = this.fileHeaders.get(fi);
        return entry.getSize();
    }

    @Override
    public long getCompressedSize(VirtualFile fi) throws IOException {
        return -1;
    }

    @Override
    public void write(@NotNull OutputStream os) throws IOException {
        SeekableInMemoryByteChannel buffer = new SeekableInMemoryByteChannel();
        try (SevenZOutputFile writer = new SevenZOutputFile(buffer)) {
            for (VirtualFile entry : this.getFiles()) {
                SevenZArchiveEntry header = new SevenZArchiveEntry();
                header.setName(entry.path);
                writer.putArchiveEntry(header);
                SeekableByteChannel data = entry.getData();
                data.position(0);
                writer.write(new SeekableByteChannelInputStream(data));
                writer.closeArchiveEntry();
            }
        }
        buffer.position(0);
        SeekableByteChannelUtils.transferTo(buffer, os);
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
