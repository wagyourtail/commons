package xyz.wagyourtail.commons.parsers.impl.semver.range;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefixedRange extends RangeData {

    protected PrefixedRange(String rawContent) {
        super(rawContent);
    }

    private PrefixedRange(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public PrefixedRange(PrefixContent content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<PrefixedRange> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c == '=' || c == '>' || c == '<' || c == '~' || c == '^' || c == '!';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            int first = reader.take();
            if (first == '=' || first == '>' || first == '<' || first == '~' || first == '^' || first == '!') {
                int second = reader.peek();
                String prefix;
                if (second == '=') {
                    reader.take();
                    prefix = (char) first + "=";
                } else {
                    prefix = String.valueOf((char) first);
                }
                Prefix p = Prefix.bySymbol.get(prefix);
                if (p == null) {
                    throw reader.createException("Invalid prefix: " + prefix);
                }
                append.append(p);
                append.append(reader.takeNonNewlineWhitespace());
                append.append(ExactVersionRange.COMPANION.read(reader));
            } else {
                throw reader.createException("Invalid prefix: " + (char) first);
            }
        }

        @Override
        public PrefixedRange unchecked(String value) {
            return new PrefixedRange(value);
        }

        @Override
        public PrefixedRange uncheckedView(CharSequence buffer, int start, int end) {
            return new PrefixedRange(buffer, start, end);
        }

        /** compat for the old parse(CharReader) call shape */
        public PrefixedRange parse(CharReader<?> reader) {
            return read(reader);
        }

        /** compat for the old parse(String) call shape */
        public PrefixedRange parse(String raw) {
            return checked(raw);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static PrefixedRange parse(String raw) {
        return COMPANION.parse(raw);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static PrefixedRange parse(CharReader<?> reader) {
        return COMPANION.parse(reader);
    }

    @Override
    public boolean contains(SemVer version) {
        PrefixContent content = (PrefixContent) getContent();
        return content.prefix.compare(content.range, version);
    }

    @Override
    protected Content checkedBuildContent(CharReader<?> reader) {
        final List<Object> list = new ArrayList<>();
        COMPANION.read(reader, new DataCompanion.Append() {
            @Override
            public void append(Object o) {
                list.add(o);
            }
        });
        Prefix prefix = null;
        ExactVersionRange range = null;
        for (Object o : list) {
            if (o instanceof Prefix) prefix = (Prefix) o;
            if (o instanceof ExactVersionRange) range = (ExactVersionRange) o;
        }
        return new PrefixContent(prefix, range);
    }

    public enum Prefix {
        EQ("=") {
            @Override
            boolean compare(ExactVersionRange range, SemVer version) {
                return range.compare(version) == 0;
            }
        },
        NEQ("!=") {
            @Override
            boolean compare(ExactVersionRange range, SemVer version) {
                return range.compare(version) != 0;
            }
        },
        GT(">") {
            @Override
            boolean compare(ExactVersionRange range, SemVer version) {
                return range.compare(version) > 0;
            }
        },
        GTE(">=") {
            @Override
            boolean compare(ExactVersionRange range, SemVer version) {
                return range.compare(version) >= 0;
            }
        },
        LT("<") {
            @Override
            boolean compare(ExactVersionRange range, SemVer version) {
                return range.compare(version) < 0;
            }
        },
        LTE("<=") {
            @Override
            boolean compare(ExactVersionRange range, SemVer version) {
                return range.compare(version) <= 0;
            }
        },
        SIMILAR("~") {
            @Override
            boolean compare(ExactVersionRange range, SemVer version) {
                return range.similar(version);
            }
        },
        COMPATIBLE("^") {
            @Override
            boolean compare(ExactVersionRange range, SemVer version) {
                return range.compatible(version);
            }
        };

        private final String symbol;

        Prefix(String symbol) {
            this.symbol = symbol;
        }

        abstract boolean compare(ExactVersionRange range, SemVer version);

        @Override
        public String toString() {
            return symbol;
        }

        public static final Map<String, Prefix> bySymbol = new HashMap<>();

        static {
            for (Prefix p : values()) {
                bySymbol.put(p.symbol, p);
            }
        }
    }

    public static class PrefixContent extends Content {
        private final Prefix prefix;
        private final ExactVersionRange range;

        public PrefixContent(Prefix prefix, ExactVersionRange range) {
            this.prefix = prefix;
            this.range = range;
        }

        public Prefix getPrefix() {
            return prefix;
        }

        public ExactVersionRange getRange() {
            return range;
        }

        @Override
        public Iterable<Object> getEntries() {
            List<Object> entries = new ArrayList<>();
            entries.add(prefix);
            entries.add(range);
            return entries;
        }
    }

}
