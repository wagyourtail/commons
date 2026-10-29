package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class Json5Array : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: ArrayContent) : super(content)

    companion object : StringDataCompanion<Json5Array>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return reader.peek() == '['
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            reader.expect('[')
            append('[')
            while (!reader.exhausted()) {
                while (!reader.exhausted()) {
                    val value = reader.parseOrNull(
                        { Json5Whitespace.read(this) },
                        { Json5Comment.read(this) }
                    )
                    if (value == null) break
                    append(value)
                }
                if (reader.peek() == ']') {
                    break
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
                if (reader.peek() == ',') {
                    append(reader.take()!!)
                } else {
                    break
                }
            }
            reader.expect(']')
            append(']')
        }

        override fun unchecked(value: String): Json5Array = Json5Array(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Json5Array =
            Json5Array(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): Json5Array = read(rawContent)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        val data = list.subList(1, list.size - 1)
        return ArrayContent(data, data.filterIsInstance<Json5Value>())
    }

    class ArrayContent(
        val data: List<Any>,
        val values: List<Json5Value>
    ) : Content() {

        override val entries: Iterable<Any>
            get() = iterable {
                yield('[')
                yieldAll(data)
                yield(']')
            }

    }

}
