package xyz.wagyourtail.commons.compress.virtualfs.impl;

import org.apache.commons.io.function.IOStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystem;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystemFactory;
import xyz.wagyourtail.commons.compress.virtualfs.file.PathFile;
import xyz.wagyourtail.commons.compress.virtualfs.listener.FileSystemChangeListener;
import xyz.wagyourtail.commons.core.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Cleaner;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class DirectoryFileSystem extends VirtualFileSystem {

    private static final Cleaner CLEANER = Cleaner.create();
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryFileSystem.class);

    @Nullable
    private final VirtualFileSystem parent;

    public DirectoryFileSystem(VirtualFile directory) throws IOException {
        super(directory);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("VirtualFile must be a directory");
        }
        this.parent = directory.parentFs;
        if (this.parent == null) {
            if (!(directory instanceof PathFile)) {
                throw new IllegalArgumentException("VirtualFile must be a PathFile or have a parent");
            }
            PathFile pathFile = (PathFile) directory;
            this.pathWatcher(pathFile.getPath());
        } else {
            this.filteredListener(directory);
        }
        if (!directory.isDirectory()) throw new IllegalArgumentException("VirtualFile must be a directory");
    }

    @Override
    protected Map<String, VirtualFile> resolveFiles() throws IOException {
        Map<String, VirtualFile> files = new HashMap<>();
        VirtualFileSystem parent = this.location.parentFs;
        if (parent == null) {
            if (!(this.location instanceof PathFile)) {
                throw new IOException("Cannot resolve files for non-path file");
            }
            PathFile path = (PathFile) this.location;
            try (Stream<Path> stream = Files.walk(path.getPath())) {
                stream.forEach(file -> {
                    String path1 = path.getPath().relativize(file).toString();
                    files.put(path1, new VirtualFile(this, path1));
                });
            }
        } else {
            IOStream.adapt(parent.getFiles(this.location.path).stream()).forEach(file -> {
                String path = file.path.substring(this.location.path.length());
                files.put(path, file);
            });
        }
        return files;
    }

    private void filteredListener(VirtualFile vf) {
        vf.parentFs.addListener(
            new FileSystemChangeListener() {
                @Override
                public void onAdded(VirtualFile file) {
                    if (file.path.startsWith(vf.path)) {
                        String path = file.path.substring(vf.path.length());
                        VirtualFile file1 = new VirtualFile(DirectoryFileSystem.this, path);
                        DirectoryFileSystem.this.files.put(path, file1);
                        DirectoryFileSystem.this.listeners.onAdded(file1);
                    }
                }

                @Override
                public void onRemoved(VirtualFile file) {
                    if (file.path.startsWith(vf.path)) {
                        String path = file.path.substring(vf.path.length());
                        VirtualFile file1 = DirectoryFileSystem.this.files.remove(path);
                        if (file1 != null) {
                            DirectoryFileSystem.this.listeners.onRemoved(file1);
                        }
                    }
                }

                @Override
                public void onModified(VirtualFile file) {
                    if (file.path.startsWith(vf.path)) {
                        String path = file.path.substring(vf.path.length());
                        VirtualFile file1 = DirectoryFileSystem.this.files.get(path);
                        if (file1 != null) {
                            DirectoryFileSystem.this.listeners.onModified(file1);
                        }
                    }
                }
            }
        );
    }

    @SuppressWarnings("unchecked")
    private void pathWatcher(Path path) throws IOException {
        // add listener for changes
        WatchService watcher = path.getFileSystem().newWatchService();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CLEANER.register(this, () -> {
            try {
                watcher.close();
                executor.shutdown();
            } catch (IOException e) {
                LOGGER.error("Failed to close watcher", e);
            }
        });
        path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        executor.submit(() -> {
            while (!isClosed()) {
                try {
                    WatchKey key = watcher.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                            LOGGER.warn("Overflowed directory watch event for {}", path);
                            continue;
                        }
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();
                        Path relative = path.relativize(filename);
                        if (ev.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            VirtualFile file = new VirtualFile(this, relative.toString());
                            this.files.put(file.path, file);
                            this.listeners.onAdded(file);
                        } else if (ev.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                            VirtualFile file = this.files.remove(relative.toString());
                            if (file != null) {
                                this.listeners.onRemoved(file);
                            }
                        } else if (ev.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            VirtualFile file = this.files.get(relative.toString());
                            if (file != null) {
                                this.listeners.onModified(file);
                            }
                        }
                    }
                    if (!key.reset()) {
                        LOGGER.warn("Failed to reset watch key for {}", path);
                        break;
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("Failed to take watch event", e);
                }
            }
        });
    }

    @Override
    protected SeekableByteChannel getDataIntl(VirtualFile fi) throws IOException {
        if (this.parent != null) {
            return this.parent.getFile(this.location.path + fi.path).getData();
        } else if (this.location instanceof PathFile) {
            PathFile path = (PathFile) this.location;
            return Files.newByteChannel(path.getPath().resolve(fi.path));
        } else {
            throw new IOException("Cannot get data for non-path file");
        }
    }

    public long getSize(VirtualFile fi) throws IOException {
        if (this.parent != null) {
            return this.parent.getFile(this.location.path + fi.path).getSize();
        } else if (this.location instanceof PathFile) {
            PathFile path = (PathFile) this.location;
            return Files.size(path.getPath().resolve(fi.path));
        } else {
            throw new IOException("Cannot get size for non-path file");
        }
    }

    @Override
    public long getCompressedSize(VirtualFile fi) throws IOException {
        if (this.parent != null) {
            return this.parent.getFile(this.location.path + fi.path).getCompressedSize();
        } else if (this.location instanceof PathFile) {
            PathFile path = (PathFile) this.location;
            return Files.size(path.getPath().resolve(fi.path));
        } else {
            throw new IOException("Cannot get compressed size for non-path file");
        }
    }

    @Override
    public void write(@NotNull OutputStream os) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected synchronized void putData(VirtualFile fi, SeekableByteChannel data) throws IOException {
        if (this.parent != null) {
            this.parent.getFile(this.location.path + fi.path).setData(data);
        } else if (location instanceof PathFile) {
            PathFile path = (PathFile) this.location;
            try (OutputStream os = Files.newOutputStream(path.getPath().resolve(fi.path), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
                IOUtils.transferTo(data, os);
            }
        } else {
            throw new IOException("Cannot put data for non-path file");
        }
        if (this.files.containsKey(fi.path)) {
            this.listeners.onModified(fi);
        } else {
            this.files.put(fi.path, fi);
            this.listeners.onAdded(fi);
        }
    }

    public static class DirectoryFileSystemFactory extends VirtualFileSystemFactory<DirectoryFileSystem> {

        @Override
        public String[] getValidMimes() {
            return new String[]{"inode/directory"};
        }

        @Override
        public DirectoryFileSystem read(VirtualFile fi) throws IOException {
            return new DirectoryFileSystem(fi);
        }

        @Override
        public DirectoryFileSystem create(String fileName) throws IOException {
            throw new UnsupportedOperationException();
        }

    }

}
