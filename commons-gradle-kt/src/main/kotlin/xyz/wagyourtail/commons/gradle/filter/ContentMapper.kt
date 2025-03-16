package xyz.wagyourtail.commons.gradle.filter

import xyz.wagyourtail.commonskt.properties.MustSet
import java.io.*
import java.nio.charset.StandardCharsets

class ContentMapper(input: Reader) : FilterReader(input) {

    var mapper: StreamMapper by MustSet()

    val changedContents: Reader by lazy {
        mapper.map(object : InputStream() {
            override fun read(): Int {
                return input.read()
            }
        }).bufferedReader(StandardCharsets.ISO_8859_1)
    }

    override fun read(): Int {
        return changedContents.read()
    }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        return changedContents.read(cbuf, off, len)
    }

    override fun skip(n: Long): Long {
        return changedContents.skip(n)
    }

    override fun ready(): Boolean {
        return changedContents.ready()
    }

    override fun markSupported(): Boolean {
        return changedContents.markSupported()
    }

    override fun mark(readAheadLimit: Int) {
        changedContents.mark(readAheadLimit)
    }

    override fun reset() {
        changedContents.reset()
    }

    override fun close() {
        changedContents.close()
    }

    interface StreamMapper {

        fun map(input: InputStream): InputStream

    }

}
