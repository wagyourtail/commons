package xyz.wagyourtail.commons.parsers.impl.semver;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;

import java.util.ArrayList;
import java.util.List;

public class VersionCore extends RawStringData implements Comparable<VersionCore> {

    protected VersionCore(String rawContent) {
        super(rawContent);
    }

    private VersionCore(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public VersionCore(VersionCoreContent content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<VersionCore> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c >= '0' && c <= '9';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            append.append(reader.takeWholeNumber());
            append.append(reader.expect('.'));
            append.append(reader.takeWholeNumber());
            append.append(reader.expect('.'));
            append.append(reader.takeWholeNumber());
        }

        @Override
        public VersionCore unchecked(String value) {
            return new VersionCore(value);
        }

        @Override
        public VersionCore uncheckedView(CharSequence buffer, int start, int end) {
            return new VersionCore(buffer, start, end);
        }

        /** compat for the old parse(CharReader) call shape */
        public VersionCore parse(CharReader<?> reader) {
            return read(reader);
        }

        /** compat for the old parse(String) call shape */
        public VersionCore parse(String raw) {
            return checked(raw);
        }

    }

    public long getMajor() {
        return ((VersionCoreContent) getContent()).major;
    }

    public long getMinor() {
        return ((VersionCoreContent) getContent()).minor;
    }

    public long getPatch() {
        return ((VersionCoreContent) getContent()).patch;
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static VersionCore parse(String raw) {
        return COMPANION.parse(raw);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static VersionCore parse(CharReader<?> reader) {
        return COMPANION.parse(reader);
    }

    @Override
    public int compareTo(VersionCore o) {
        int c = Long.compare(getMajor(), o.getMajor());
        if (c != 0) return c;
        c = Long.compare(getMinor(), o.getMinor());
        if (c != 0) return c;
        return Long.compare(getPatch(), o.getPatch());
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
        return new VersionCoreContent((Long) list.get(0), (Long) list.get(2), (Long) list.get(4));
    }

    public static class VersionCoreContent extends Content {
        private final long major;
        private final long minor;
        private final long patch;

        public VersionCoreContent(long major, long minor, long patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        public long getMajor() {
            return major;
        }

        public long getMinor() {
            return minor;
        }

        public long getPatch() {
            return patch;
        }

        @Override
        public Iterable<Object> getEntries() {
            List<Object> entries = new ArrayList<>();
            entries.add(major);
            entries.add('.');
            entries.add(minor);
            entries.add('.');
            entries.add(patch);
            return entries;
        }
    }

}
