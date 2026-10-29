package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class Json5ObjectEntry : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: ObjectEntry) : super(content)

    companion object : StringDataCompanion<Json5ObjectEntry>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return Json5ObjectKey.shouldRead(reader)
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            append(Json5ObjectKey.read(reader))
            while (!reader.exhausted()) {
                val value = reader.parseOrNull(
                    { Json5Whitespace.read(this) },
                    { Json5Comment.read(this) }
                )
                if (value == null) break
                append(value)
            }
            reader.expect(':')
            append(':')
            while (!reader.exhausted()) {
                val value = reader.parseOrNull(
                    { Json5Whitespace.read(this) },
                    { Json5Comment.read(this) }
                )
                if (value == null) break
                append(value)
            }
            append(Json5Value.read(reader))
        }

        override fun unchecked(value: String): Json5ObjectEntry = Json5ObjectEntry(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Json5ObjectEntry =
            Json5ObjectEntry(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): Json5ObjectEntry = read(rawContent)

    }

    val key: String
        get() = (content as ObjectEntry).key.value

    val value: Json5Value
        get() = (content as ObjectEntry).value

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        val sep = list.indexOf(':')
        return ObjectEntry(
            list.first() as Json5ObjectKey,
            list.subList(1, sep),
            list.subList(sep + 1, list.size - 1),
            list.last() as Json5Value
        )
    }

    class ObjectEntry(
        val key: Json5ObjectKey,
        val postKey: List<Any>,
        val preValue: List<Any>,
        val value: Json5Value
    ) : Content() {

        override val entries: Iterable<Any>
            get() = iterable {
                yield(key)
                yieldAll(postKey)
                yield(":")
                yieldAll(preValue)
                yield(value)
            }

    }

}
