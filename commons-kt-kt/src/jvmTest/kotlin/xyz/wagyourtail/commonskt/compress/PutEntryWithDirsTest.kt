package xyz.wagyourtail.commonskt.compress

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PutEntryWithDirsTest {

    private fun build(entries: List<String>): ByteArray {
        val dirs = mutableSetOf<String>()
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zip ->
            for (name in entries) {
                zip.putEntryWithDirs(name, dirs, CONSTANT_TIME_FOR_ZIP_ENTRIES) {
                    it.write("x".toByteArray())
                }
            }
        }
        return bos.toByteArray()
    }

    @Test
    fun `synthesizes parent dir entries`() {
        val names = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(build(listOf("com/foo/Bar.class")))).use {
            var e = it.nextEntry
            while (e != null) { names += e.name; e = it.nextEntry }
        }
        assertEquals(listOf("com/", "com/foo/", "com/foo/Bar.class"), names)
    }

    @Test
    fun `dedupes shared parents and skips explicit dir entries`() {
        val names = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(
            build(listOf("a/b/one.txt", "a/b/two.txt", "a/c/")))).use {
            var e = it.nextEntry
            while (e != null) { names += e.name; e = it.nextEntry }
        }
        assertEquals(listOf("a/", "a/b/", "a/b/one.txt", "a/b/two.txt", "a/c/"), names)
    }

    @Test
    fun `entries are readable and timestamped`() {
        val tmp = createTempFile(suffix = ".zip").toFile()
        tmp.writeBytes(build(listOf("d/f.txt")))
        ZipFile(tmp).use { zip ->
            val e = zip.getEntry("d/f.txt")
            assertEquals("x", zip.getInputStream(e).readBytes().decodeToString())
            assertEquals(CONSTANT_TIME_FOR_ZIP_ENTRIES, e.time)
            assertTrue(zip.getEntry("d/").isDirectory)
        }
    }

}
