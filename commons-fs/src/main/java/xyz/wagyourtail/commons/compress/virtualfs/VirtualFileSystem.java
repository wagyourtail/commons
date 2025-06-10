package xyz.wagyourtail.commons.compress.virtualfs;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.wagyourtail.commons.compress.virtualfs.listener.FileSystemChangeListener;
import xyz.wagyourtail.commons.compress.virtualfs.listener.FileSystemChangeListeners;
import xyz.wagyourtail.commons.core.io.FastWrapOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class VirtualFileSystem implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualFileSystem.class);

    public final VirtualFile location;

    protected final Map<String, VirtualFile> files = new HashMap<>();
    protected final Map<String, Set<VirtualFile>> directories = new ConcurrentHashMap<>();
    protected final FileSystemChangeListeners listeners = new FileSystemChangeListeners();
    Map<VirtualFile, SeekableByteChannel> pendingWrites = new HashMap<>();
    private volatile boolean read = false;
    @Getter
    private boolean closed = false;

    public VirtualFileSystem(VirtualFile location) {
        this.location = location;
    }

    public void addListener(FileSystemChangeListener listener) {
        this.listeners.addListener(listener);
    }

    public boolean removeListener(FileSystemChangeListener listener) {
        return this.listeners.removeListener(listener);
    }

    public void clearListeners() {
        this.listeners.clearListeners();
    }

    protected abstract Map<String, VirtualFile> resolveFiles() throws IOException;

    public String getNormalizedFileName() {
        return this.location.normalizedFileName();
    }

    public String nameWithoutExtension() {
        String name = this.getNormalizedFileName();
        if (name.contains(".")) name = name.substring(0, name.lastIndexOf('.'));
        return name;
    }

    SeekableByteChannel getData(VirtualFile fi) throws IOException {
        if (this.pendingWrites.containsKey(fi)) {
            return this.pendingWrites.get(fi);
        }
        if (this.files.containsKey(fi.path)) {
            return this.getDataIntl(fi);
        }
        return null;
    }

    SeekableByteChannel getExtraData(VirtualFile fi) {
        if (this.files.containsKey(fi.path)) {
            return this.getExtraDataIntl(fi);
        }
        return null;
    }

    protected abstract SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException;

    protected SeekableByteChannel getExtraDataIntl(VirtualFile fi) {
        return null;
    }

    public abstract long getSize(VirtualFile fi) throws IOException;

    public abstract long getCompressedSize(VirtualFile fi) throws IOException;

    protected synchronized void putData(VirtualFile fi, SeekableByteChannel data) throws IOException {
        this.pendingWrites.put(fi, data);
        if (this.files.containsKey(fi.path)) {
            this.listeners.onModified(fi);
        } else {
            this.files.put(fi.path, fi);
            this.listeners.onAdded(fi);
        }
    }

    public synchronized boolean hasPendingWrites() {
        return !this.pendingWrites.isEmpty();
    }

    private void resolveFilesIntl() throws IOException {
        if (this.closed) throw new IllegalStateException("ArchiveFile is closed");
        if (!this.read) {
            synchronized (this.files) {
                if (!this.read) {
                    this.read = true;
                    this.files.putAll(this.resolveFiles());
                    this.directories.put("", new HashSet<>());
                    for (VirtualFile fi : this.files.values()) {
                        VirtualFile parent = fi.getParent();
                        while (parent != null) {
                            this.directories.computeIfAbsent(parent.path, k -> new HashSet<>()).add(fi);
                            fi = parent;
                            parent = fi.getParent();
                        }
                    }
                }
            }
        }
    }

    public final Set<String> listFiles() throws IOException {
        this.resolveFilesIntl();
        return Collections.unmodifiableSet(new HashSet<>(this.files.keySet()));
    }

    public final Collection<VirtualFile> getFiles() throws IOException {
        this.resolveFilesIntl();
        return Collections.unmodifiableCollection(this.files.values());
    }

    public final Collection<VirtualFile> getFiles(String prefix) throws IOException {
        this.resolveFilesIntl();
        return this.getFiles().stream().filter(fi -> fi.path.startsWith(prefix)).collect(Collectors.toList());
    }

    public final VirtualFile getFile(String path) throws IOException {
        this.resolveFilesIntl();
        return this.files.getOrDefault(path, new VirtualFile(this, path));
    }

    public final Set<VirtualFile> listDirectory(VirtualFile dir) throws IOException {
        return this.listDirectory(dir.path);
    }

    public final Set<VirtualFile> listDirectory(String dir) throws IOException {
        this.resolveFilesIntl();
        return this.directories.computeIfAbsent(dir, (d) -> {
            LOGGER.warn("Directory {} does not exist", d);
            return Collections.emptySet();
        });
    }

    @Override
    public synchronized void close() throws IOException {
        this.files.clear();
        this.directories.clear();
        this.closed = true;
    }

    public SeekableByteChannel write() throws IOException {
        FastWrapOutputStream baos = new FastWrapOutputStream();
        this.write(baos);
        return baos.wrap();
    }

    public abstract void write(@NotNull OutputStream os) throws IOException;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VirtualFileSystem) {
            VirtualFileSystem other = (VirtualFileSystem) obj;
            return this.location.equals(other.location);
        }
        return false;
    }

}
