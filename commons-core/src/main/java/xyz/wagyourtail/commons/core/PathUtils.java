package xyz.wagyourtail.commons.core;


import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.commons.core.function.IOConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
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

}
