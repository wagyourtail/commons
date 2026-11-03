package xyz.wagyourtail.commonskt.jvm

import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.ZipOutputStream

fun File.deleteIfExists() {
    if (exists()) {
        delete()
    }
}

@Throws(IOException::class)
fun Path.isZip(): Boolean {
    Files.newInputStream(this).use { stream ->
        val header = ByteArray(4)
        val i = stream.read(header, 0, 4)
        if (i != 4) return false
        val pk = header[0].toInt() == 0x50 && header[1].toInt() == 0x4B
        val zip = header[2].toInt() == 0x3 && header[3].toInt() == 0x4
        val zip2 = header[2].toInt() == 0x5 && header[3].toInt() == 0x6
        return pk && (zip || zip2)
    }
}

fun Path.openZipFileSystem(create: Boolean = true): FileSystem {
    if (create && !Files.exists(this)) {
        ZipOutputStream(Files.newOutputStream(this)).close()
    }
    return FileSystems.newFileSystem(URI.create("jar:${toUri()}"), emptyMap<String, Any>(), null)
}

/** Move this file onto [dest] atomically where the FS supports it,
 *  replacing any existing file. */
@Throws(IOException::class)
fun Path.moveAtomicTo(dest: Path) {
    try {
        Files.move(this, dest, StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING)
    } catch (_: AtomicMoveNotSupportedException) {
        Files.move(this, dest, StandardCopyOption.REPLACE_EXISTING)
    }
}

/** A fresh temp file next to this path for write-then-move producers. */
@Throws(IOException::class)
fun Path.tempSibling(): Path {
    val parent = toAbsolutePath().parent
    parent?.let { Files.createDirectories(it) }
    return Files.createTempFile(parent, ".${fileName}.", ".tmp")
}

/** Copy this file to a temp sibling of [dest], then atomic-move it in —
 *  [dest] never observes a truncated/partial file. */
@Throws(IOException::class)
fun Path.copyAtomicTo(dest: Path) {
    val tmp = dest.tempSibling()
    try {
        Files.copy(this, tmp, StandardCopyOption.REPLACE_EXISTING)
        tmp.moveAtomicTo(dest)
    } finally {
        Files.deleteIfExists(tmp)
    }
}
