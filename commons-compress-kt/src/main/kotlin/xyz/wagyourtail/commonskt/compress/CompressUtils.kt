package xyz.wagyourtail.commonskt.compress

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

fun Path.readZipContents(): List<String> {
    val contents = mutableListOf<String>()
    forEachInZip { entry, _ ->
        contents.add(entry)
    }
    return contents
}

fun Path.forEachInZip(action: (String, InputStream) -> Unit) {
    Files.newByteChannel(this).use { sbc ->
        ZipFile.builder().setIgnoreLocalFileHeader(true).setSeekableByteChannel(sbc).get().use { zip ->
            for (zipArchiveEntry in zip.entries.iterator()) {
                if (zipArchiveEntry.isDirectory) {
                    continue
                }
//                if (zipArchiveEntry.name.isEmpty() && zipArchiveEntry.size == 0L) {
//                    continue
//                }
                zip.getInputStream(zipArchiveEntry).use {
                    action(zipArchiveEntry.name, it)
                }
            }
        }
    }
}

fun Path.forEntryInZip(action: (ZipArchiveEntry, InputStream) -> Unit) {
    Files.newByteChannel(this).use { sbc ->
        ZipFile.builder().setIgnoreLocalFileHeader(true).setSeekableByteChannel(sbc).get().use { zip ->
            for (zipArchiveEntry in zip.entries.iterator()) {
                if (zipArchiveEntry.isDirectory) {
                    continue
                }
//                if (zipArchiveEntry.name.isEmpty() && zipArchiveEntry.size == 0L) {
//                    continue
//                }
                zip.getInputStream(zipArchiveEntry).use {
                    action(zipArchiveEntry, it)
                }
            }
        }
    }
}

fun <T> Path.readZipInputStreamFor(path: String, throwIfMissing: Boolean = true, action: (InputStream) -> T): T {
    Files.newByteChannel(this).use {
        ZipFile.builder().setIgnoreLocalFileHeader(true).setSeekableByteChannel(it).get().use { zip ->
            val entry = zip.getEntry(path.replace("\\", "/"))
            if (entry != null) {
                return zip.getInputStream(entry).use(action)
            } else {
                if (throwIfMissing) {
                    throw IllegalArgumentException("Missing file $path in $this")
                }
            }
        }
    }
    return null as T
}

fun Path.zipContains(path: String): Boolean {
    Files.newByteChannel(this).use {
        ZipFile.builder().setIgnoreLocalFileHeader(true).setSeekableByteChannel(it).get().use { zip ->
            val entry = zip.getEntry(path.replace("\\", "/"))
            if (entry != null) {
                return true
            }
        }
    }
    return false
}