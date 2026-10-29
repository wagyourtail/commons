package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.data.Content
import xyz.wagyourtail.commonskt.data.RawStringData
import xyz.wagyourtail.commonskt.data.StringDataCompanion
import xyz.wagyourtail.commonskt.reader.CharReader

class Json5Comment : RawStringData {

    constructor(rawContent: String) : super(rawContent)

    private constructor(view: CharSequence, start: Int, end: Int) : super(view, start, end)

    constructor(content: Content) : super(content.toString())

    companion object : StringDataCompanion<Json5Comment>() {

        override fun shouldRead(reader: CharReader<*>): Boolean {
            val c = reader.peek()
            if (c != '/') return false
            val copy = reader.copy(1)
            val n = copy.take()
            return n == '/' || n == '*'
        }

        override fun read(reader: CharReader<*>, append: (Any) -> Unit) {
            reader.parse(
                "comment",
                {
                    append(expect("//"))
                    append(takeUntil("\n"))
                    if (!exhausted()) {
                        append(expect('\n'))
                    }
                },
                {
                    append(expect("/*"))
                    append(takeUntil("*/"))
                    append(expect("*/"))
                }
            )
        }

        override fun unchecked(value: String): Json5Comment = Json5Comment(value)

        override fun uncheckedView(buffer: CharSequence, start: Int, end: Int): Json5Comment =
            Json5Comment(buffer, start, end)

        /** compat for the old StringDataBuilder call shape */
        operator fun invoke(rawContent: CharReader<*>): Json5Comment = read(rawContent)

    }

    override fun checkedBuildContent(reader: CharReader<*>): Content {
        return Companion.buildContent(reader)
    }

}
