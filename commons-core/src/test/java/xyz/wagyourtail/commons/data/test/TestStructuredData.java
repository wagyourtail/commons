package xyz.wagyourtail.commons.data.test;

import org.junit.jupiter.api.Test;
import xyz.wagyourtail.commons.core.data.BinaryDataCompanion;
import xyz.wagyourtail.commons.core.data.BuildBytesVisitor;
import xyz.wagyourtail.commons.core.data.BuildStringVisitor;
import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.ParseCache;
import xyz.wagyourtail.commons.core.data.ParsedBinaryData;
import xyz.wagyourtail.commons.core.data.ParsedStringData;
import xyz.wagyourtail.commons.core.data.RawBinaryData;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.ByteArrayByteReader;
import xyz.wagyourtail.commons.core.reader.ByteReader;
import xyz.wagyourtail.commons.core.reader.CharAccepter;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.ParseException;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.core.writer.ByteArrayByteWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Java mirror of the Kotlin structured-data model tests: view-node
 * laziness/zero-copy, synthetic serialize, companions, and ParseCache —
 * using a tiny {@code ident=ident} grammar (text) and a
 * {@code [u1 len][bytes]} leaf + {@code [tag][BinIdent]} composite (binary).
 */
public class TestStructuredData {

    private static final CharAccepter LETTER = new CharAccepter() {
        @Override
        public boolean accept(char c) {
            return Character.isLetter(c);
        }
    };

    private static List<Object> entries(Content c) {
        List<Object> out = new ArrayList<Object>();
        for (Object o : c.getEntries()) {
            out.add(o);
        }
        return out;
    }

    // ---------- text grammar ----------

    public static class Ident extends RawStringData {

        protected Ident(String raw) {
            super(raw);
        }

        private Ident(CharSequence view, int start, int end) {
            super(view, start, end);
        }

        public String getValue() {
            return (String) ((Content.SingleContent) getContent()).getValue();
        }

        /** derived part memoized via ParseCache (the atomic-leaf story) */
        public int getLength() {
            return COMPANION.cached(getRawContent(), new ParseCache.Compute<Integer>() {
                @Override
                public Integer compute() {
                    Companion.cacheComputes++;
                    return getRawContent().length();
                }
            });
        }

        @Override
        protected Content checkedBuildContent(CharReader<?> reader) {
            Companion.buildCount++;
            return new Content.SingleContent(reader.takeRemaining());
        }

        public static final Companion COMPANION = new Companion();

        public static class Companion extends StringDataCompanion<Ident> {

            static int buildCount;
            static int viewCount;
            static int cacheComputes;

            @Override
            public boolean shouldRead(CharReader<?> reader) {
                return Character.isLetter(reader.peek());
            }

            @Override
            public void read(CharReader<?> reader, DataCompanion.Append append) {
                append.append(reader.takeWhile(LETTER));
            }

            @Override
            public Ident unchecked(String value) {
                return new Ident(value);
            }

            @Override
            public Ident uncheckedView(CharSequence buffer, int start, int end) {
                viewCount++;
                return new Ident(buffer, start, end);
            }

        }

    }

    public static class Pair extends RawStringData {

        protected Pair(String raw) {
            super(raw);
        }

        private Pair(CharSequence view, int start, int end) {
            super(view, start, end);
        }

        public Ident getKey() {
            return (Ident) entries(getContent()).get(0);
        }

        public Ident getValue() {
            return (Ident) entries(getContent()).get(2);
        }

        @Override
        protected Content checkedBuildContent(CharReader<?> reader) {
            Ident k = Ident.COMPANION.read(reader);
            reader.expect("=");
            Ident v = Ident.COMPANION.read(reader);
            return new Content.ListContent(Arrays.<Object>asList(k, "=", v));
        }

        public static final Companion COMPANION = new Companion();

        public static class Companion extends StringDataCompanion<Pair> {

            static int viewCount;

            @Override
            public boolean shouldRead(CharReader<?> reader) {
                return Ident.COMPANION.shouldRead(reader);
            }

            @Override
            public void read(CharReader<?> reader, DataCompanion.Append append) {
                append.append(Ident.COMPANION.read(reader));
                reader.expect("=");
                append.append("=");
                append.append(Ident.COMPANION.read(reader));
            }

            @Override
            public Pair unchecked(String value) {
                return new Pair(value);
            }

            @Override
            public Pair uncheckedView(CharSequence buffer, int start, int end) {
                viewCount++;
                return new Pair(buffer, start, end);
            }

        }

    }

    public static class SyntheticGreeting extends ParsedStringData {

        private SyntheticGreeting(Content content) {
            super(content);
        }

        public static SyntheticGreeting of(String name) {
            return new SyntheticGreeting(
                new Content.ListContent(Arrays.<Object>asList("hello ", name, "!"))
            );
        }

    }

    // ---------- binary grammar ----------

    public static class BinIdent extends RawBinaryData {

        protected BinIdent(byte[] raw) {
            super(raw);
        }

        private BinIdent(byte[] view, int start, int end) {
            super(view, start, end);
        }

        public byte[] getPayload() {
            return (byte[]) entries(getContent()).get(1);
        }

        @Override
        protected Content checkedBuildContent(ByteReader<?> reader) {
            Companion.buildCount++;
            int len = reader.takeU1();
            return new Content.ListContent(Arrays.<Object>asList(len, reader.take(len)));
        }

        public static final Companion COMPANION = new Companion();

        public static class Companion extends BinaryDataCompanion<BinIdent> {

            static int buildCount;
            static int viewCount;

            @Override
            public boolean shouldRead(ByteReader<?> reader) {
                return reader.peek() > 0;
            }

            @Override
            public void read(ByteReader<?> reader, DataCompanion.Append append) {
                int len = reader.takeU1();
                append.append((byte) len);
                append.append(reader.take(len));
            }

            @Override
            public BinIdent unchecked(byte[] value) {
                return new BinIdent(value);
            }

            @Override
            public BinIdent uncheckedView(byte[] buffer, int start, int end) {
                viewCount++;
                return new BinIdent(buffer, start, end);
            }

        }

    }

    public static class BinRecord extends RawBinaryData {

        protected BinRecord(byte[] raw) {
            super(raw);
        }

        private BinRecord(byte[] view, int start, int end) {
            super(view, start, end);
        }

        public int getTag() {
            return (Integer) entries(getContent()).get(0);
        }

        public BinIdent getIdent() {
            return (BinIdent) entries(getContent()).get(1);
        }

        @Override
        protected Content checkedBuildContent(ByteReader<?> reader) {
            int tag = reader.takeU1();
            return new Content.ListContent(Arrays.<Object>asList(tag, BinIdent.COMPANION.read(reader)));
        }

        public static final Companion COMPANION = new Companion();

        public static class Companion extends BinaryDataCompanion<BinRecord> {

            @Override
            public boolean shouldRead(ByteReader<?> reader) {
                return reader.peek() != -1;
            }

            @Override
            public void read(ByteReader<?> reader, DataCompanion.Append append) {
                int tag = reader.takeU1();
                append.append((byte) tag);
                append.append(BinIdent.COMPANION.read(reader));
            }

            @Override
            public BinRecord unchecked(byte[] value) {
                return new BinRecord(value);
            }

            @Override
            public BinRecord uncheckedView(byte[] buffer, int start, int end) {
                return new BinRecord(buffer, start, end);
            }

        }

    }

    public static class SyntheticBinRecord extends ParsedBinaryData {

        private SyntheticBinRecord(Content content) {
            super(content);
        }

        public static SyntheticBinRecord of(int tag, byte[] payload) {
            return new SyntheticBinRecord(new Content.ListContent(
                Arrays.<Object>asList((byte) tag, (byte) payload.length, payload)
            ));
        }

    }

    // ---------- tests ----------

    @Test
    public void testAtomicLeaf() {
        Ident id = Ident.COMPANION.checked("abc");
        assertEquals("abc", id.getRawContent());
        assertEquals("abc", id.getValue());
    }

    @Test
    public void testViewNodesAreZeroCopy() {
        CharReader<?> reader = new StringCharReader("key=value;rest");
        Pair pair = Pair.COMPANION.read(reader);
        assertEquals("key=value", pair.getRawContent());
        assertEquals(";rest", reader.takeRemaining());
        assertEquals("key", pair.getKey().getRawContent());
        assertEquals("value", pair.getValue().getRawContent());
        assertEquals("=", entries(pair.getContent()).get(1));
    }

    @Test
    public void testReadUsesViewCtorOnBackedReader() {
        Ident.Companion.viewCount = 0;
        Pair.Companion.viewCount = 0;
        Pair.COMPANION.checked("a=b");
        assertEquals(1, Pair.Companion.viewCount);
        // children replay as views too — 2 from each read grammar + 2 from
        // the content rebuild
        entries(Pair.COMPANION.checked("x=y").getContent());
        assertEquals(2, Pair.Companion.viewCount);
        assertEquals(6, Ident.Companion.viewCount);
    }

    @Test
    public void testContentParsesOnce() {
        Ident.Companion.buildCount = 0;
        Ident id = Ident.COMPANION.checked("abc");
        assertEquals(0, Ident.Companion.buildCount);
        Content c1 = id.getContent();
        assertEquals(1, Ident.Companion.buildCount);
        assertSame(c1, id.getContent());
        assertEquals(1, Ident.Companion.buildCount);
    }

    @Test
    public void testSyntheticSerializes() {
        SyntheticGreeting s = SyntheticGreeting.of("world");
        assertEquals("hello world!", s.getRawContent());
        assertEquals("hello world!", BuildStringVisitor.apply(s));
    }

    @Test
    public void testCheckedRejectsTrailing() {
        assertThrows(ParseException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                Pair.COMPANION.checked("a=b!");
            }
        });
    }

    @Test
    public void testUncheckedTrustsRaw() {
        Ident id = Ident.COMPANION.unchecked("not-an-ident!!!");
        assertEquals("not-an-ident!!!", id.getRawContent());
    }

    @Test
    public void testCompanionReadInsideParse() {
        CharReader<?> reader = new StringCharReader("key=value");
        Pair pair = reader.parse(new StringCharReader.ElementReader<Pair>() {
            @Override
            public Pair read(CharReader<?> reader) {
                return Pair.COMPANION.read(reader);
            }
        });
        assertEquals("key=value", pair.getRawContent());
        assertEquals("", reader.takeRemaining());
    }

    @Test
    public void testEqualsAcrossStorageFlavors() {
        Ident a = Ident.COMPANION.unchecked("key");
        Ident b = Pair.COMPANION.checked("key=v").getKey();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testParseCache() {
        ParseCache.clear();
        Ident.Companion.cacheComputes = 0;
        Ident.COMPANION.unchecked("abc").getLength();
        Ident.COMPANION.unchecked("abc").getLength();   // second instance — cached
        assertEquals(1, Ident.Companion.cacheComputes);
        Ident.COMPANION.unchecked("abcd").getLength();  // different raw — recompute
        assertEquals(2, Ident.Companion.cacheComputes);
    }

    // ---------- binary ----------

    @Test
    public void testBinaryLeaf() {
        BinIdent id = BinIdent.COMPANION.checked(new byte[]{3, 'a', 'b', 'c'});
        assertArrayEquals(new byte[]{3, 97, 98, 99}, id.getRawContent());
        assertArrayEquals(new byte[]{97, 98, 99}, id.getPayload());
    }

    @Test
    public void testBinaryView() {
        BinIdent.Companion.viewCount = 0;
        byte[] input = new byte[]{2, 'h', 'i', 7};
        ByteArrayByteReader reader = new ByteArrayByteReader(input);
        BinIdent id = BinIdent.COMPANION.read(reader);
        assertEquals(1, BinIdent.Companion.viewCount);
        assertArrayEquals(new byte[]{2, 104, 105}, id.getRawContent());
        assertEquals(7, reader.take());
        // writeBytesTo streams the view range — no slice, exact bytes
        ByteArrayByteWriter out = new ByteArrayByteWriter();
        id.writeBytesTo(out);
        assertArrayEquals(new byte[]{2, 104, 105}, out.toByteArray());
    }

    @Test
    public void testBinaryComposite() {
        BinRecord record = BinRecord.COMPANION.checked(new byte[]{1, 3, 'a', 'b', 'c'});
        assertEquals(1, record.getTag());
        assertArrayEquals(new byte[]{3, 97, 98, 99}, record.getIdent().getRawContent());
        assertArrayEquals(new byte[]{97, 98, 99}, record.getIdent().getPayload());
    }

    @Test
    public void testBinarySynthetic() {
        SyntheticBinRecord s = SyntheticBinRecord.of(7, new byte[]{1, 2, 3});
        assertArrayEquals(new byte[]{7, 3, 1, 2, 3}, s.getRawContent());
        assertArrayEquals(new byte[]{7, 3, 1, 2, 3}, BuildBytesVisitor.apply(s));
    }

    @Test
    public void testBinaryContentParsesOnce() {
        BinIdent.Companion.buildCount = 0;
        BinIdent id = BinIdent.COMPANION.checked(new byte[]{1, 65});
        assertEquals(0, BinIdent.Companion.buildCount);
        id.getContent();
        assertEquals(1, BinIdent.Companion.buildCount);
        id.getContent();
        assertEquals(1, BinIdent.Companion.buildCount);
    }

    @Test
    public void testBinaryCheckedRejectsTrailing() {
        assertThrows(ParseException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                BinIdent.COMPANION.checked(new byte[]{1, 65, 0, 0});
            }
        });
    }

    @Test
    public void testBinaryEquals() {
        BinIdent a = BinIdent.COMPANION.unchecked(new byte[]{1, 65});
        BinIdent b = BinIdent.COMPANION.checked(new byte[]{1, 65});
        assertEquals(a, b);
    }

}
