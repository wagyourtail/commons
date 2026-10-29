package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class Json5Object : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: ObjectContent) : super(content)

    companion object : StringDataCompanion<Json5Object>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            return reader.peek() == '{'
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            reader.expect('{')
            append('{')
            while (!reader.exhausted()) {
                while (!reader.exhausted()) {
                    val value = reader.parseOrNull(
                        { Json5Whitespace.read(this) },
                        { Json5Comment.read(this) }
                    )
                    if (value == null) break
                    append(value)
                }
                if (reader.peek() == '}') {
                    break
                }

                append(Json5ObjectEntry.read(reader))

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
            reader.expect('}')
            append('}')
        }

        override fun unchecked(value: String): Json5Object = Json5Object(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Json5Object =
            Json5Object(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): Json5Object = read(rawContent)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        val list = mutableListOf<Any>()
        Companion.read(reader) { list.add(it) }
        return ObjectContent(list.subList(1, list.size - 1).toMutableList())
    }

    class ObjectContent(
        val data: MutableList<Any>,
        val entryMap: MutableMap<String, Json5ObjectEntry>
    ) : Content() {

        constructor(data: MutableList<Any>) : this(data, mutableMapOf()) {
            for (entry in data) {
                if (entry is Json5ObjectEntry) {
                    entryMap[entry.key] = entry
                }
            }
        }

        override val entries: Iterable<Any>
            get() = iterable {
                yield('{')
                yieldAll(data)
                yield('}')
            }

    }

}
