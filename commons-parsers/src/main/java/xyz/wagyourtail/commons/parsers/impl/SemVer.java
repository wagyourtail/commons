package xyz.wagyourtail.commons.parsers.impl;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.RawStringData;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.semver.Build;
import xyz.wagyourtail.commons.parsers.impl.semver.PreRelease;
import xyz.wagyourtail.commons.parsers.impl.semver.VersionCore;

import java.util.ArrayList;
import java.util.List;

public class SemVer extends RawStringData implements Comparable<SemVer> {

    protected SemVer(String rawContent) {
        super(rawContent);
    }

    private SemVer(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public SemVer(SemVerContent content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<SemVer> {

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return c >= '0' && c <= '9';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            append.append(VersionCore.COMPANION.read(reader));
            if (reader.peek() == '-') {
                append.append((char) reader.take());
                append.append(PreRelease.COMPANION.read(reader));
            }
            if (reader.peek() == '+') {
                append.append((char) reader.take());
                append.append(Build.COMPANION.read(reader));
            }
        }

        @Override
        public SemVer unchecked(String value) {
            return new SemVer(value);
        }

        @Override
        public SemVer uncheckedView(CharSequence buffer, int start, int end) {
            return new SemVer(buffer, start, end);
        }

        /** compat for the old parse(CharReader) call shape */
        public SemVer parse(CharReader<?> reader) {
            return read(reader);
        }

        /** compat for the old parse(String) call shape */
        public SemVer parse(String raw) {
            return checked(raw);
        }

    }

    public VersionCore getCore() {
        return ((SemVerContent) getContent()).core;
    }

    public PreRelease getPreRelease() {
        return ((SemVerContent) getContent()).preRelease;
    }

    public Build getBuild() {
        return ((SemVerContent) getContent()).build;
    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static SemVer parse(String raw) {
        return COMPANION.parse(raw);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static SemVer parse(CharReader<?> reader) {
        return COMPANION.parse(reader);
    }

    @Override
    public int compareTo(SemVer o) {
        int c = getCore().compareTo(o.getCore());
        if (c != 0) return c;
        if ((getPreRelease() != null) != (o.getPreRelease() != null)) {
            // NOTE: preserved from the legacy impl — a version without a
            // pre-release sorts before one with (inverted vs the spec)
            return getPreRelease() == null ? -1 : 1;
        }
        if (getPreRelease() != null) {
            return getPreRelease().compareTo(o.getPreRelease());
        }
        return 0;
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
        PreRelease preRelease = null;
        Build build = null;
        for (Object o : list) {
            if (o instanceof PreRelease) preRelease = (PreRelease) o;
            if (o instanceof Build) build = (Build) o;
        }
        return new SemVerContent((VersionCore) list.get(0), preRelease, build);
    }

    public static class SemVerContent extends Content {
        private final VersionCore core;
        private final PreRelease preRelease;
        private final Build build;

        public SemVerContent(VersionCore core, PreRelease preRelease, Build build) {
            this.core = core;
            this.preRelease = preRelease;
            this.build = build;
        }

        public VersionCore getCore() {
            return core;
        }

        public PreRelease getPreRelease() {
            return preRelease;
        }

        public Build getBuild() {
            return build;
        }

        @Override
        public Iterable<Object> getEntries() {
            List<Object> entries = new ArrayList<>();
            entries.add(core);
            if (preRelease != null) {
                entries.add('-');
                entries.add(preRelease);
            }
            if (build != null) {
                entries.add('+');
                entries.add(build);
            }
            return entries;
        }
    }

}
