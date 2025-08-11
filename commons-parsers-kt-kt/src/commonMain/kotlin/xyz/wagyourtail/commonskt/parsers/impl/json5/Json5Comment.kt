package xyz.wagyourtail.commonskt.parsers.impl.json5

import xyz.wagyourtail.commonskt.parsers.Data
import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.reader.CharReader

class Json5Comment(rawContent: String) : StringData.OnlyRaw<Data.ListContent>(rawContent) {

    constructor(content: Data.ListContent) : this(content.toString())

    companion object : StringDataBuilder<Json5Comment, Data.ListContent> {

        override fun invoke(rawContent: CharReader<*>): Json5Comment {
            return Json5Comment(checkedBuildContent(rawContent))
        }

        override fun checkedBuildContent(reader: CharReader<*>): ListContent {
            return ListContent(
                reader.parse(
                "comment",
                {
                    buildList {
                        add(expect("//"))
                        add(takeUntil("\n"))
                        if (!exhausted()) {
                            add(expect('\n'))
                        }
                    }
                },
                {
                    buildList {
                        add(expect("/*"))
                        add(takeUntil("*/"))
                        add(expect("*/"))
                    }
                }
            ))
        }
    }

    override fun checkedBuildContent(reader: CharReader<*>): ListContent {
        return Companion.checkedBuildContent(reader)
    }

}