package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class Json5ObjectEntry(content: ObjectEntry) : StringData.OnlyParsed<Json5ObjectEntry.ObjectEntry>(content) {

    companion object : StringDataBuilder<Json5ObjectEntry, ObjectEntry> {

        override fun invoke(rawContent: CharReader<*>): Json5ObjectEntry {
            return Json5ObjectEntry(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): ObjectEntry {
            val key = Json5ObjectKey(reader)
            val postKey = buildList {
                while(!reader.exhausted()) {
                    val value = reader.parseOrNull(
                        Json5Whitespace::invoke,
                        Json5Comment::invoke
                    )
                    if (value == null) break
                    add(value)
                }
            }
            reader.expect(':')
            val preValue = buildList {
                while(!reader.exhausted()) {
                    val value = reader.parseOrNull(
                        Json5Whitespace::invoke,
                        Json5Comment::invoke
                    )
                    if (value == null) break
                    add(value)
                }
            }
            val value = Json5Value(reader)
            return ObjectEntry(key, postKey, preValue, value)
        }

    }

    val key: String
        get() = content.key.value

    val value: Json5Value
        get() = content.value

    class ObjectEntry(
        val key: Json5ObjectKey,
        val postKey: List<Any>,
        val preValue: List<Any>,
        val value: Json5Value
    ) : Content() {

        override val entries: Iterable<*>
            get() = iterable {
                yield(key)
                yieldAll(postKey)
                yield(":")
                yieldAll(preValue)
                yield(value)
            }

    }

}