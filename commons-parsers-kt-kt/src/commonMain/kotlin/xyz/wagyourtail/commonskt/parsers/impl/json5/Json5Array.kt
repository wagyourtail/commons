package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class Json5Array(content: ArrayContent) : StringData.OnlyParsed<Json5Array.ArrayContent>(content) {

    companion object : StringDataBuilder<Json5Array, ArrayContent> {

        override fun invoke(rawContent: CharReader<*>): Json5Array {
            return Json5Array(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): ArrayContent {
            val entries = mutableListOf<Any>()
            val values = mutableListOf<Json5Value>()
            reader.expect('[')
            while (!reader.exhausted()) {
                while (!reader.exhausted()) {
                    val value = reader.parseOrNull(
                        Json5Whitespace::invoke,
                        Json5Comment::invoke
                    )
                    if (value == null) break
                    entries.add(value)
                }
                if (reader.peek() == ']') {
                    break
                }

                val entry = Json5Value(reader)
                entries.add(entry)
                values.add(entry)

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
            reader.expect(']')
            return ArrayContent(entries, values)
        }

    }

    class ArrayContent(
        val data: List<Any>,
        val values: List<Json5Value>
    ) : Content() {

        override val entries: Iterable<*>
            get() = iterable {
                yield('[')
                yieldAll(data)
                yield(']')
            }

    }

}