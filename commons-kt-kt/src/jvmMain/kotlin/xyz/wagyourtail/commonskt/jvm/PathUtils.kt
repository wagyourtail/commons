package xyz.wagyourtail.commonskt.jvm

import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
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
