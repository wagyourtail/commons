package xyz.wagyourtail.commonskt.compress

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

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

/**
 * Writes [name] to the stream, synthesizing any parent `dir/` entries
 * not already in [emittedDirs] first.
 *
 * Explicit directory entries are required for package resource
 * enumeration — `ClassLoader.getResources("com/foo")` only sees a
 * package if the `com/foo/` entry exists. Plugin systems scanning
 * packages (log4j2's `packages=`, FML directory discovery) fail
 * silently against jars that omit them.
 *
 * @param name entry path (`/` separated)
 * @param emittedDirs tracks already-written dir entries; mutated as
 *        parents are emitted — share one set across a whole jar write
 * @param time entry timestamp; [CONSTANT_TIME_FOR_ZIP_ENTRIES] for
 *        deterministic output
 * @param write body writer, invoked with the open stream
 */
fun java.util.zip.ZipOutputStream.putEntryWithDirs(
    name: String,
    emittedDirs: MutableSet<String>,
    time: Long = 0L,
    write: (java.io.OutputStream) -> Unit
) {
    var i = name.indexOf('/')
    while (i != -1) {
        val dir = name.substring(0, i + 1)
        if (emittedDirs.add(dir)) {
            putNextEntry(java.util.zip.ZipEntry(dir).apply { this.time = time })
            closeEntry()
        }
        i = name.indexOf('/', i + 1)
    }
    // explicit `dir/` input entry — the parent loop already emitted
    // it; writing it again would be a duplicate
    if (name.endsWith('/')) return
    putNextEntry(java.util.zip.ZipEntry(name).apply { this.time = time })
    write(this)
    closeEntry()
}
