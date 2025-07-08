package xyz.wagyourtail.commonskt.parsers.impl

import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.parsers.impl.json5.Json5Comment
import xyz.wagyourtail.commonskt.parsers.impl.json5.Json5Value
import xyz.wagyourtail.commonskt.parsers.impl.json5.Json5Whitespace
import xyz.wagyourtail.commonskt.reader.CharReader
import xyz.wagyourtail.commonskt.utils.iterable

class Json5File(content: FileContent) : StringData.OnlyParsed<Json5File.FileContent>(content) {

    companion object : StringDataBuilder<Json5File, FileContent> {

        override fun invoke(rawContent: CharReader<*>): Json5File {
            return Json5File(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): FileContent {
            val preContent = buildList {
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
            val postContent = buildList {
                while(!reader.exhausted()) {
                    val value = reader.parseOrNull(
                        Json5Whitespace::invoke,
                        Json5Comment::invoke
                    )
                    if (value == null) break
                    add(value)
                }
            }
            return FileContent(preContent, value, postContent)
        }

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