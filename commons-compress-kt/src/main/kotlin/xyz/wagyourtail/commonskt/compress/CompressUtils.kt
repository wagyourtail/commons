package xyz.wagyourtail.commonskt.compress

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Calendar
import java.util.GregorianCalendar

fun Path.listZipContents(): List<String> {
    val contents = mutableListOf<String>()
    forEachInZip { entry, _ ->
        contents.add(entry)
    }
    return contents
}

fun Path.safeOpenZipFile(): ZipFile {
    return ZipFile.builder().setIgnoreLocalFileHeader(true).setSeekableByteChannel(Files.newByteChannel(this)).get()
}

inline fun Path.forEachInZip(crossinline action: (String, () -> InputStream) -> Unit) {
    forEntryInZip { entry, inputStream ->
        action(entry.name, inputStream)
    }
}

fun Path.forEntryInZip(action: (ZipArchiveEntry, () -> InputStream) -> Unit) {
    safeOpenZipFile().use { zip ->
        for (zipArchiveEntry in zip.entries.iterator()) {
            if (zipArchiveEntry.isDirectory) {
                continue
            }

            action(zipArchiveEntry) { zip.getInputStream(zipArchiveEntry) }
        }
    }
}

fun <T> Path.readZipInputStreamFor(path: String, throwIfMissing: Boolean = true, action: (InputStream) -> T): T {
    safeOpenZipFile().use { zip ->
        val entry = zip.getEntry(path.replace("\\", "/"))
        if (entry != null) {
            return zip.getInputStream(entry).use(action)
        } else {
            if (throwIfMissing) {
                throw IllegalArgumentException("Missing file $path in $this")
            }
        }
    }
    return null as T
}

fun Path.zipContains(path: String): Boolean {
    safeOpenZipFile().use { zip ->
        val entry = zip.getEntry(path.replace("\\", "/"))
        if (entry != null) {
            return true
        }
    }
    return false
}

val CONSTANT_TIME_FOR_ZIP_ENTRIES = GregorianCalendar(1980, Calendar.FEBRUARY, 1, 0, 0, 0).timeInMillis
