package xyz.wagyourtail.commonskt.parsers.impl.json5.test

import xyz.wagyourtail.commonskt.parsers.StringData
import xyz.wagyourtail.commonskt.parsers.impl.Json5File
import kotlin.test.Test
import kotlin.test.assertEquals


class Json5Test {
    @Test
    fun testJson5Spec() {
        val file: Json5File = Json5File.checked(SPEC)
        assertEquals(SPEC, StringData.BuildStringVisitor.apply(file))
    }

    @Test
    fun testJson5Multiline() {
        val file: Json5File = Json5File.checked(MULTILINE)
        assertEquals(MULTILINE, StringData.BuildStringVisitor.apply(file))
    }

    companion object {
        private val SPEC = """
            {
              // comments
              unquoted: 'and you can quote me on that',
              singleQuotes: 'I can use "double quotes" here',
              lineBreaks: "Look, Mom! \
            No \\n's!",
              hexadecimal: 0xdecaf,
              leadingDecimalPoint: .8675309, andTrailing: 8675309.,
              positiveSign: +1,
              trailingComma: 'in objects', andIn: ['arrays',],
              "backwardsCompatible": "with JSON",
            }
            """.trimIndent()

        private val MULTILINE = """
            {
              cr: "new\
            line",
              lf: "new\
            line",
              crlf: "new\
            line",
              u2028: "new line",
              u2029: "new line",
              escaped: "new\nline",
            }
            """.trimIndent()
    }
}
