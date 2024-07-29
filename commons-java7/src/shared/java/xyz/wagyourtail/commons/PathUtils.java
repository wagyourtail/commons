package xyz.wagyourtail.commons;

import org.apache.commons.io.function.IOConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipOutputStream;

public class PathUtils {

    public static void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

// fun Path.forEachFile(action: (Path) -> Unit) {
//    Files.walkFileTree(this, object: SimpleFileVisitor<Path>() {
//        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
//            action(file)
//            return FileVisitResult.CONTINUE
//        }
//    })
//}

    public static void forEachFile(Path path, IOConsumer<Path> visitor) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
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
            return header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04;
        }
    }

    public static FileSystem openZipFileSystem(Path path, boolean create) throws IOException {
        if (create && !Files.exists(path)) {
            new ZipOutputStream(Files.newOutputStream(path)).close();
        }
        return FileSystems.newFileSystem(path, null);
    }

}
