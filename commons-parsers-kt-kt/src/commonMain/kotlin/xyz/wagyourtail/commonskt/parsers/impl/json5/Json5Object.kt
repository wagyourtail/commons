package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class Json5Object(content: ObjectContent) : StringData.OnlyParsed<Json5Object.ObjectContent>(content) {

    companion object : StringDataBuilder<Json5Object, ObjectContent> {

        override fun invoke(rawContent: CharReader<*>): Json5Object {
            return Json5Object(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): ObjectContent {
            val entries = mutableListOf<Any>()
            val entryMap = mutableMapOf<String, Json5ObjectEntry>()
            reader.expect('{')
            while (!reader.exhausted()) {
                while (!reader.exhausted()) {
                    val value = reader.parseOrNull(
                            Json5Whitespace::invoke,
                            Json5Comment::invoke
                    )
                    if (value == null) break
                    entries.add(value)
                }
                if (reader.peek() == '}') {
                    break
                }

                val entry = Json5ObjectEntry(reader)
                entries.add(entry)
                entryMap[entry.key] = entry

                while (!reader.exhausted()) {
                    val value = reader.parseOrNull(
                            Json5Whitespace::invoke,
                            Json5Comment::invoke
                    )
                    if (value == null) break
                    entries.add(value)
                }
                if (reader.peek() == ',') {
                    entries.add(reader.take()!!)
                } else {
                    break
                }
            }
            reader.expect('}')
            return ObjectContent(entries)
        }

    }

    class ObjectContent(
        val data: MutableList<Any>,
        val entryMap: MutableMap<String, Json5ObjectEntry>
    ) : Content() {

        constructor(data: MutableList<Any>): this(data, mutableMapOf()) {
            for (entry in data) {
                if (entry is Json5ObjectEntry) {
                    entryMap[entry.key] = entry
                }
            }
        }

        override val entries: Iterable<*>
            get() = iterable {
                yield('{')
                yieldAll(data)
                yield('}')
            }

    }

}