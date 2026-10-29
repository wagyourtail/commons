package xyz.wagyourtail.commons.parsers.impl;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.semver.range.OrCombinedRange;
import xyz.wagyourtail.commons.parsers.impl.semver.range.RangeData;

import java.util.ArrayList;
import java.util.List;

public class SemVerRange extends RangeData {

    protected SemVerRange(String rawContent) {
        super(rawContent);
    }

    private SemVerRange(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public SemVerRange(Content.SingleContent content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<SemVerRange> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            return reader.peek() != -1;
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            append.append(OrCombinedRange.COMPANION.read(reader));
        }

        @Override
        public SemVerRange unchecked(String value) {
            return new SemVerRange(value);
        }

        @Override
        public SemVerRange uncheckedView(CharSequence buffer, int start, int end) {
            return new SemVerRange(buffer, start, end);
        }

        /** compat for the old parse(CharReader) call shape */
        public SemVerRange parse(CharReader<?> reader) {
            return read(reader);
        }

        /** compat for the old parse(String) call shape */
        public SemVerRange parse(String raw) {
            return checked(raw);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static SemVerRange parse(String raw) {
        return COMPANION.parse(raw);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static SemVerRange parse(CharReader<?> reader) {
        return COMPANION.parse(reader);
    }

    @Override
    public boolean contains(SemVer version) {
        return ((RangeData) ((Content.SingleContent) getContent()).getValue()).contains(version);
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
        return new Content.SingleContent(list.get(0));
    }

}
