package xyz.wagyourtail.commons.core;


import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.commons.core.function.IOConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipOutputStream;

public class PathUtils {

    private PathUtils() {
    }

    public static void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @NotNull
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }

            @NotNull
            @Override
            public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void forEachFile(Path path, final IOConsumer<Path> visitor) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @NotNull
            @Override
            public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                visitor.accept(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static boolean isZip(Path path) throws IOException {
        try (InputStream stream = Files.newInputStream(path)) {
            byte[] header = new byte[4];
            int i = stream.read(header, 0, 4);
            if (i != 4) return false;
            boolean pk = header[0] == 0x50 && header[1] == 0x4B;
            boolean zip = header[2] == 0x03 && header[3] == 0x04;
            boolean zip2 = header[2] == 0x05 && header[3] == 0x06;
            return pk && (zip || zip2);
        }
    }

    public static FileSystem openZipFileSystem(Path path, boolean create) throws IOException {
        if (create && !Files.exists(path)) {
            new ZipOutputStream(Files.newOutputStream(path)).close();
        }
        return FileSystems.newFileSystem(path, null);
    }

    /**
     * Move {@code src} onto {@code dest} atomically where the FS supports
     * it, replacing any existing file.
     */
    public static void moveAtomic(Path src, Path dest) throws IOException {
        try {
            Files.move(src, dest, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * A fresh temp file next to {@code dest} for write-then-move producers:
     * produced files must never leave a truncated/partial file at the
     * destination.
     */
    public static Path tempSibling(Path dest) throws IOException {
        Path parent = dest.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        return Files.createTempFile(parent, "." + dest.getFileName() + ".", ".tmp");
    }

    /**
     * Copy {@code src} to a temp sibling of {@code dest}, then atomic-move
     * it in — {@code dest} never observes a truncated/partial file.
     */
    public static void copyAtomic(Path src, Path dest) throws IOException {
        Path tmp = tempSibling(dest);
        try {
            Files.copy(src, tmp, StandardCopyOption.REPLACE_EXISTING);
            moveAtomic(tmp, dest);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

}
