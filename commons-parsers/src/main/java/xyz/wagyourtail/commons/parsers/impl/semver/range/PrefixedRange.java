package xyz.wagyourtail.commons.parsers.impl.semver.range;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

public class PrefixedRange extends RangeData<PrefixedRange.PrefixContent> {

    public PrefixedRange(PrefixedRange.PrefixContent content) {
        super(content);
    }

    public static PrefixedRange parse(String raw) {
        val reader = new StringCharReader(raw);
        val content = contentBuilder(reader);
        reader.expectEOS();
        return new PrefixedRange(content);
    }

    public static PrefixedRange parse(CharReader<?> reader) {
        return new PrefixedRange(contentBuilder(reader));
    }

    public static PrefixContent contentBuilder(CharReader<?> reader) {
        val first = reader.take();
        if (first == '=' || first == '>' || first == '<' || first == '~' || first == '^' || first == '!') {
            val second = reader.peek();
            String prefix;
            if (second == '=') {
                reader.take();
                prefix = String.valueOf(new char[] { (char)first, (char)second });
            } else {
                prefix = Character.toString((char)first);
            }
            val p = Prefix.bySymbol.get(prefix);
            if (p == null) {
                throw reader.createException("Invalid prefix: " + prefix);
            }
            reader.takeNonNewlineWhitespace();
            return new PrefixContent(p, ExactVersionRange.parse(reader));
        } else {
            throw reader.createException("Invalid prefix: " + (char) first);
        }
    }

    @Override
    public boolean contains(SemVer version) {
        val content = getContent();
        return content.getPrefix().compare.test(content.getRange(), version);
    }

    @Getter
    @AllArgsConstructor
    public static class PrefixContent extends Data.Content<Object> {
        private final Prefix prefix;
        private final ExactVersionRange range;

        @Override
        public Iterable<Object> getEntries() {
            return Arrays.asList(prefix, range);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Prefix {
        EQ("=", (r, v) -> r.compare(v) == 0),
        NEQ("!=", (r, v) -> r.compare(v) != 0),
        GT(">", (r, v) -> r.compare(v) > 0),
        GTE(">=", (r, v) -> r.compare(v) >= 0),
        LT("<", (r, v) -> r.compare(v) < 0),
        LTE("<=", (r, v) -> r.compare(v) <= 0),
        SIMILAR("~", ExactVersionRange::similar),
        COMPATIBLE("^", ExactVersionRange::compatible)
        ;

        private static final Map<String, Prefix> bySymbol = new HashMap<>();
        static {
            for (Prefix prefix : values()) {
                bySymbol.put(prefix.symbol, prefix);
            }
        }

        private final String symbol;
        private final BiPredicate<ExactVersionRange, SemVer> compare;

        @Override
        public String toString() {
            return symbol;
        }
    }

}
