package xyz.wagyourtail.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

public class HashUtils {

    public static boolean testSha1(long size, String sha1, Path path) throws IOException, NoSuchAlgorithmException {
        return testSha1(size, sha1, path, Duration.ofDays(1));
    }

    public static boolean testSha1(long size, String sha1, Path path, Duration expireTime) throws IOException, NoSuchAlgorithmException {
        if (path.toFile().exists()) {
            if (path.toFile().length() == size || size == -1L) {
                if (sha1.isEmpty()) {
                    // fallback: expire if older than a day
                    return path.toFile().lastModified() > System.currentTimeMillis() - expireTime.toMillis();
                }
                 return getSha1(path).equalsIgnoreCase(sha1);
            }
        }
        return false;
    }

    public static String getSha1(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digestSha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream is = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digestSha1.update(buffer, 0, read);
            }
        }
        byte[] hashBytes = digestSha1.digest();
        StringBuilder hash = new StringBuilder();
        for (byte b : hashBytes) {
            hash.append(String.format("%02x", b));
        }
        return hash.toString();
    }

    public static String getSha1(File file) throws IOException, NoSuchAlgorithmException {
        return getSha1(file.toPath());
    }

    public static String getShortSha1(Path path) throws IOException, NoSuchAlgorithmException {
        return getSha1(path).substring(0, 7);
    }

    public static String getShortSha1(File file) throws IOException, NoSuchAlgorithmException {
        return getSha1(file).substring(0, 7);
    }

}
