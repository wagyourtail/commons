package xyz.wagyourtail.commonskt.test.data

import xyz.wagyourtail.commonskt.data.BinaryDataCompanion
import xyz.wagyourtail.commonskt.data.BuildBytesVisitor
import xyz.wagyourtail.commonskt.data.BuildStringVisitor
import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.ListContent
import xyz.wagyourtail.commonskt.data.ParseCache
import xyz.wagyourtail.commonskt.data.ParsedBinaryData
import xyz.wagyourtail.commonskt.data.ParsedStringData
import xyz.wagyourtail.commonskt.data.RawBinaryData
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.SingleContent
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.ByteReader
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.reader.ParseException
import xyz.wagyourtail.commonskt.writer.ByteArrayByteWriter
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * Tests for the unified structured-data model: view-node laziness/zero-copy,
 * synthetic serialize, companions, and ParseCache — using a tiny `ident=ident`
 * grammar (text) and a `[u1 len][bytes]` leaf + `[tag][BinIdent]` composite
 * (binary).
 */
class TestStructuredData {

    // ---------- text grammar ----------

    class Ident : RawStringData {

        constructor(raw: String) : super(raw)

        private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

        val value: String
            get() = (content as SingleContent).value as String

        /** derived part memoized via ParseCache (the atomic-leaf story) */
        val length: Int
            get() = Companion.cached(rawContent) { cacheComputes++; it.length }

        companion object : StringDataCompanion<Ident>() {

            var buildCount = 0
            var viewCount = 0
            var cacheComputes = 0

            override fun shouldRead(reader: CharReader<*>): Boolean =
                reader.peek()?.isLetter() == true

            override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
                append(reader.takeWhile { it.isLetter() })
            }

            override fun unchecked(value: String): Ident = Ident(value)

            override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Ident {
                viewCount++
                return Ident(buffer, start, end)
            }

        }

        override fun checkedBuildContent(reader: CharReader<*>): Content {
            buildCount++
            return SingleContent(reader.takeRemaining())
        }

    }

    class Pair : RawStringData {

        constructor(raw: String) : super(raw)

        private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

        val key: Ident
            get() = content.entries.toList()[0] as Ident

        val value: Ident
            get() = content.entries.toList()[2] as Ident

        companion object : StringDataCompanion<Pair>() {

            var viewCount = 0

            override fun shouldRead(reader: CharReader<*>): Boolean = Ident.shouldRead(reader)

            override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
                append(Ident.read(reader))
                reader.expect("=")
                append("=")
                append(Ident.read(reader))
            }

            override fun unchecked(value: String): Pair = Pair(value)

            override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Pair {
                viewCount++
                return Pair(buffer, start, end)
            }

        }

        override fun checkedBuildContent(reader: CharReader<*>): Content {
            val k = Ident.read(reader)
            reader.expect("=")
            val v = Ident.read(reader)
            return ListContent(listOf(k, "=", v))
        }

    }

    class SyntheticGreeting(content: Content) : ParsedStringData(content) {

        companion object {

            fun of(name: String) = SyntheticGreeting(ListContent(listOf("hello ", name, "!")))

        }

    }

    // ---------- binary grammar ----------

    class BinIdent : RawBinaryData {

        constructor(raw: ByteArray) : super(raw)

        private constructor(view: ByteArray, start: Int, end: Int) : super(view, start, end)

        val payload: ByteArray
            get() = content.entries.toList()[1] as ByteArray

        companion object : BinaryDataCompanion<BinIdent>() {

            var buildCount = 0
            var viewCount = 0

            override fun shouldRead(reader: ByteReader<*>): Boolean = (reader.peek() ?: 0) > 0

            override fun read(reader: ByteReader<*>, append: (Any) -> Unit) {
                val len = reader.takeU1()
                append(len.toByte())
                append(reader.take(len))
            }

            override fun unchecked(value: ByteArray): BinIdent = BinIdent(value)

            override fun uncheckedView(buffer: ByteArray, start: Int, end: Int): BinIdent {
                viewCount++
                return BinIdent(buffer, start, end)
            }

        }

        override fun checkedBuildContent(reader: ByteReader<*>): Content {
            buildCount++
            val len = reader.takeU1()
            return ListContent(listOf(len, reader.take(len)))
        }

    }

    class BinRecord : RawBinaryData {

        constructor(raw: ByteArray) : super(raw)

        private constructor(view: ByteArray, start: Int, end: Int) : super(view, start, end)

        val tag: Int
            get() = content.entries.first() as Int

        val ident: BinIdent
            get() = content.entries.toList()[1] as BinIdent

        companion object : BinaryDataCompanion<BinRecord>() {

            override fun shouldRead(reader: ByteReader<*>): Boolean = reader.peek() != null

            override fun read(reader: ByteReader<*>, append: (Any) -> Unit) {
                val tag = reader.takeU1()
                append(tag.toByte())
                append(BinIdent.read(reader))
            }

            override fun unchecked(value: ByteArray): BinRecord = BinRecord(value)

            override fun uncheckedView(buffer: ByteArray, start: Int, end: Int): BinRecord =
                BinRecord(buffer, start, end)

        }

        override fun checkedBuildContent(reader: ByteReader<*>): Content {
            val tag = reader.takeU1()
            return ListContent(listOf(tag, BinIdent.read(reader)))
        }

    }

    class SyntheticBinRecord(content: Content) : ParsedBinaryData(content) {

        companion object {

            fun of(tag: Int, payload: ByteArray) = SyntheticBinRecord(
                ListContent(listOf(tag.toByte(), payload.size.toByte(), payload))
            )

        }

    }

    // ---------- tests ----------

    @Test
    fun testAtomicLeaf() {
        val id = Ident.checked("abc")
        assertEquals("abc", id.rawContent)
        assertEquals("abc", id.value)
    }

    @Test
    fun testViewNodesAreZeroCopy() {
        val reader = CharReader("key=value;rest")
        val pair = Pair.read(reader)
        // the node is a view over the consumed range — the rest of the
        // input is untouched and unconsumed
        assertEquals("key=value", pair.rawContent)
        assertEquals(";rest", reader.takeRemaining())
        // children are themselves range views into the shared buffer
        assertEquals("key", pair.key.rawContent)
        assertEquals("value", pair.value.rawContent)
        assertEquals("=", pair.content.entries.toList()[1])
    }

    @Test
    fun testReadUsesViewCtorOnBackedReader() {
        Ident.viewCount = 0
        Pair.viewCount = 0
        Pair.checked("a=b")
        assertEquals(1, Pair.viewCount)
        // children replay as views too — 2 from each read grammar + 2 from
        // the content rebuild
        Pair.checked("x=y").content.entries.toList()
        assertEquals(2, Pair.viewCount)
        assertEquals(6, Ident.viewCount)
    }

    @Test
    fun testContentParsesOnce() {
        Ident.buildCount = 0
        val id = Ident.checked("abc")
        assertEquals(0, Ident.buildCount)
        val c1 = id.content
        assertEquals(1, Ident.buildCount)
        assertSame(c1, id.content)
        assertEquals(1, Ident.buildCount)
    }

    @Test
    fun testSyntheticSerializes() {
        val s = SyntheticGreeting.of("world")
        assertEquals("hello world!", s.rawContent)
        assertEquals("hello world!", BuildStringVisitor.apply(s))
    }

    @Test
    fun testCheckedRejectsTrailing() {
        assertFailsWith<ParseException> { Pair.checked("a=b!") }
        assertFailsWith<ParseException> { Ident.checked("ab1") }
    }

    @Test
    fun testUncheckedTrustsRaw() {
        // no validation — the caller is trusted
        val id = Ident.unchecked("not-an-ident!!!")
        assertEquals("not-an-ident!!!", id.rawContent)
    }

    @Test
    fun testCompanionReadInsideParse() {
        val reader = CharReader("key=value")
        val pair = reader.parse { Pair.read(this) }
        assertEquals("key=value", pair.rawContent)
        assertEquals("", reader.takeRemaining())
    }

    @Test
    fun testEqualsAcrossStorageFlavors() {
        val a = Ident.unchecked("key")
        val b = Pair.checked("key=v").key
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun testParseCache() {
        ParseCache.clear()
        Ident.cacheComputes = 0
        Ident.unchecked("abc").length
        Ident.unchecked("abc").length   // second instance, same raw — cached
        assertEquals(1, Ident.cacheComputes)
        Ident.unchecked("abcd").length  // different raw — recompute
        assertEquals(2, Ident.cacheComputes)
    }

    // ---------- binary ----------

    @Test
    fun testBinaryLeaf() {
        val id = BinIdent.checked(byteArrayOf(3, 'a'.code.toByte(), 'b'.code.toByte(), 'c'.code.toByte()))
        assertContentEquals(byteArrayOf(3, 97, 98, 99), id.rawContent)
        assertContentEquals(byteArrayOf(97, 98, 99), id.payload)
    }

    @Test
    fun testBinaryView() {
        BinIdent.viewCount = 0
        val input = byteArrayOf(2, 'h'.code.toByte(), 'i'.code.toByte(), 7)
        val reader = ByteReader(input)
        val id = BinIdent.read(reader)
        assertEquals(1, BinIdent.viewCount)
        assertContentEquals(byteArrayOf(2, 104, 105), id.rawContent)
        assertEquals(7, reader.take())
        // writeBytesTo streams the view range — no slice, exact bytes
        val out = ByteArrayByteWriter()
        id.writeBytesTo(out)
        assertContentEquals(byteArrayOf(2, 104, 105), out.toByteArray())
    }

    @Test
    fun testBinaryComposite() {
        val record = BinRecord.checked(byteArrayOf(1, 3, 'a'.code.toByte(), 'b'.code.toByte(), 'c'.code.toByte()))
        assertEquals(1, record.tag)
        assertContentEquals(byteArrayOf(3, 97, 98, 99), record.ident.rawContent)
        assertContentEquals(byteArrayOf(97, 98, 99), record.ident.payload)
    }

    @Test
    fun testBinarySynthetic() {
        val s = SyntheticBinRecord.of(7, byteArrayOf(1, 2, 3))
        assertContentEquals(byteArrayOf(7, 3, 1, 2, 3), s.rawContent)
        assertContentEquals(byteArrayOf(7, 3, 1, 2, 3), BuildBytesVisitor.apply(s))
    }

    @Test
    fun testBinaryContentParsesOnce() {
        BinIdent.buildCount = 0
        val id = BinIdent.checked(byteArrayOf(1, 65))
        assertEquals(0, BinIdent.buildCount)
        id.content
        assertEquals(1, BinIdent.buildCount)
        id.content
        assertEquals(1, BinIdent.buildCount)
    }

    @Test
    fun testBinaryCheckedRejectsTrailing() {
        assertFailsWith<ParseException> { BinIdent.checked(byteArrayOf(1, 65, 0, 0)) }
    }

    @Test
    fun testBinaryEquals() {
        val a = BinIdent.unchecked(byteArrayOf(1, 65))
        val b = BinIdent.checked(byteArrayOf(1, 65))
        assertEquals(a, b)
        assertIs<BinIdent>(b)
    }

}
