package xyz.wagyourtail.commons.parsers.impl.json5.test;

import org.junit.jupiter.api.Test;
import xyz.wagyourtail.commons.parsers.StringData;
import xyz.wagyourtail.commons.parsers.impl.Json5File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Json5Test {

    private static final String SPEC = """
            {
              // comments
              unquoted: 'and you can quote me on that',
              singleQuotes: 'I can use "double quotes" here',
              lineBreaks: "Look, Mom! \\
            No \\\\n's!",
              hexadecimal: 0xdecaf,
              leadingDecimalPoint: .8675309, andTrailing: 8675309.,
              positiveSign: +1,
              trailingComma: 'in objects', andIn: ['arrays',],
              "backwardsCompatible": "with JSON",
            }
            """.stripIndent();

    private static final String MULTILINE = """
            {
              cr: "new\\
            line",
              lf: "new\\
            line",
              crlf: "new\\
            line",
              u2028: "new line",
              u2029: "new line",
              escaped: "new\\nline",
            }
            """;

    @Test
    public void testJson5Spec() {
        Json5File file = Json5File.parse(SPEC);
        assertEquals(SPEC, StringData.BuildStringVisitor.apply(file));
    }

    @Test
    public void testJson5Multiline() {
        Json5File file = Json5File.parse(MULTILINE);
        assertEquals(MULTILINE, StringData.BuildStringVisitor.apply(file));
    }

}
