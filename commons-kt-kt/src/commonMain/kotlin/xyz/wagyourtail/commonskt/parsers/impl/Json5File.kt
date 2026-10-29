package xyz.wagyourtail.commonskt.parsers.impl

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.parsers.impl.json5.Json5Comment
import xyz.wagyourtail.commonskt.parsers.impl.json5.Json5Value
import xyz.wagyourtail.commonskt.parsers.impl.json5.Json5Whitespace
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class Json5File : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: FileContent) : super(content)

    companion object : StringDataCompanion<Json5File>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return reader.peek() != null
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            while (!reader.exhausted()) {
                val value = reader.parseOrNull(
                    { Json5Whitespace.read(this) },
                    { Json5Comment.read(this) }
                )
                if (value == null) break
                append(value)
            }
            append(Json5Value.read(reader))
            while (!reader.exhausted()) {
                val value = reader.parseOrNull(
                    { Json5Whitespace.read(this) },
                    { Json5Comment.read(this) }
                )
                if (value == null) break
                append(value)
            }
        }

        override fun unchecked(value: String): Json5File = Json5File(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Json5File =
            Json5File(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): Json5File = read(rawContent)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        val vi = list.indexOfFirst { it is Json5Value }
        return FileContent(
            list.subList(0, vi),
            list[vi] as Json5Value,
            list.subList(vi + 1, list.size)
        )
    }

    class FileContent(
        val preContent: List<Any>,
        val value: Json5Value,
        val postContent: List<Any>
    ) : Content() {

        override val entries: Iterable<Any>
            get() = iterable {
                yieldAll(preContent)
                yield(value)
                yieldAll(postContent)
            }

    }
}
