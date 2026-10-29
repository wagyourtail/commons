package xyz.wagyourtail.commons.parsers.impl.semver.range;

import xyz.wagyourtail.commons.core.data.Content;
import xyz.wagyourtail.commons.core.data.DataCompanion;
import xyz.wagyourtail.commons.core.data.StringDataCompanion;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.ArrayList;
import java.util.List;

public class ExactVersionRange extends RangeData {

    protected ExactVersionRange(String rawContent) {
        super(rawContent);
    }

    private ExactVersionRange(CharSequence view, int start, int end) {
        super(view, start, end);
    }

    public ExactVersionRange(VersionContent content) {
        super(content);
    }

    public static final Companion COMPANION = new Companion();

    public static class Companion extends StringDataCompanion<ExactVersionRange> {

        private static String readSegment(CharReader<?> reader) {
            return reader.parse(
                    (e) -> String.valueOf(e.takeWholeNumber(false)),
                    (e) -> String.valueOf(e.expect("wildcard", c -> c == '*' || c == 'x' || c == 'X'))
            );
        }

        @Override
        public boolean shouldRead(CharReader<?> reader) {
            int c = reader.peek();
            return (c >= '0' && c <= '9') || c == '*' || c == 'x' || c == 'X';
        }

        @Override
        public void read(CharReader<?> reader, Append append) {
            append.append(readSegment(reader));
            if (reader.peek() != '.') return;
            append.append((char) reader.take());
            append.append(readSegment(reader));
            if (reader.peek() != '.') return;
            append.append((char) reader.take());
            append.append(readSegment(reader));
        }

        @Override
        public ExactVersionRange unchecked(String value) {
            return new ExactVersionRange(value);
        }

        @Override
        public ExactVersionRange uncheckedView(CharSequence buffer, int start, int end) {
            return new ExactVersionRange(buffer, start, end);
        }

        /** compat for the old parse(CharReader) call shape */
        public ExactVersionRange parse(CharReader<?> reader) {
            return read(reader);
        }

        /** compat for the old parse(String) call shape */
        public ExactVersionRange parse(String raw) {
            return checked(raw);
        }

    }

    /** @deprecated use {@link Companion#checked(String)} */
    @Deprecated
    public static ExactVersionRange parse(String raw) {
        return COMPANION.parse(raw);
    }

    /** @deprecated use {@link Companion#read(CharReader)} */
    @Deprecated
    public static ExactVersionRange parse(CharReader<?> reader) {
        return COMPANION.parse(reader);
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
        return new VersionContent(
            String.valueOf(list.get(0)),
            list.size() > 2 ? String.valueOf(list.get(2)) : null,
            list.size() > 4 ? String.valueOf(list.get(4)) : null
        );
    }

    @Override
    public boolean contains(SemVer version) {
        return compare(version) == 0;
    }

    public boolean equals(SemVer version) {
        return contains(version);
    }

    public int compare(SemVer version) {
        VersionContent content = (VersionContent) getContent();
        String major = content.major;
        String minor = content.minor;
        String patch = content.patch;

        if (major != null && (major.equals("*") || major.equalsIgnoreCase("x"))) major = null;
        if (minor != null && (minor.equals("*") || minor.equalsIgnoreCase("x"))) minor = null;
        if (patch != null && (patch.equals("*") || patch.equalsIgnoreCase("x"))) patch = null;

        VersionCoreWrapper versionCore = new VersionCoreWrapper(version);
        long versionMajor = versionCore.major;

        if (major == null) return 0;
        long majorLong = Long.parseLong(major);
        if (versionMajor < majorLong) return -1;
        if (versionMajor > majorLong) return 1;

        if (minor == null) return 0;
        long versionMinor = versionCore.minor;
        long minorLong = Long.parseLong(minor);
        if (versionMinor < minorLong) return -1;
        if (versionMinor > minorLong) return 1;

        if (patch == null) return 0;
        long versionPatch = versionCore.patch;
        long patchLong = Long.parseLong(patch);
        int patchResult = Long.compare(patchLong, versionPatch);
        if (patchResult == 0 && version.getPreRelease() != null) {
            return -1;
        }
        return patchResult;
    }

    public boolean similar(SemVer version) {
        VersionContent content = (VersionContent) getContent();
        String major = content.major;
        String minor = content.minor;
        String patch = content.patch;

        if (major != null && (major.equals("*") || major.equalsIgnoreCase("x"))) major = null;
        if (minor != null && (minor.equals("*") || minor.equalsIgnoreCase("x"))) minor = null;
        if (patch != null && (patch.equals("*") || patch.equalsIgnoreCase("x"))) patch = null;

        VersionCoreWrapper versionCore = new VersionCoreWrapper(version);

        if (major == null) return true;
        long majorLong = Long.parseLong(major);
        if (versionCore.major != majorLong) return false;

        if (minor == null) return true;
        long minorLong = Long.parseLong(minor);
        if (versionCore.minor != minorLong) return false;

        if (patch == null) return true;
        long patchLong = Long.parseLong(patch);
        return versionCore.patch >= patchLong;
    }

    public boolean compatible(SemVer version) {
        VersionContent content = (VersionContent) getContent();
        String major = content.major;
        String minor = content.minor;
        String patch = content.patch;

        if (major != null && (major.equals("*") || major.equalsIgnoreCase("x"))) major = null;
        if (minor != null && (minor.equals("*") || minor.equalsIgnoreCase("x"))) minor = null;
        if (patch != null && (patch.equals("*") || patch.equalsIgnoreCase("x"))) patch = null;

        VersionCoreWrapper versionCore = new VersionCoreWrapper(version);

        if (major == null) return true;
        long majorLong = Long.parseLong(major);
        if (versionCore.major != majorLong) return false;

        if (minor == null) return true;
        long versionMinor = versionCore.minor;
        long minorLong = Long.parseLong(minor);
        if (majorLong == 0 && versionMinor != minorLong) return false;
        if (versionMinor < minorLong) return false;
        if (versionMinor > minorLong) return true;

        if (patch == null) return true;
        long versionPatch = versionCore.patch;
        long patchLong = Long.parseLong(patch);
        if (minorLong == 0 && versionPatch != patchLong) return false;
        return versionPatch >= patchLong;
    }

    private static class VersionCoreWrapper {
        private final long major;
        private final long minor;
        private final long patch;

        private VersionCoreWrapper(SemVer version) {
            xyz.wagyourtail.commons.parsers.impl.semver.VersionCore core = version.getCore();
            this.major = core.getMajor();
            this.minor = core.getMinor();
            this.patch = core.getPatch();
        }
    }

    public static class VersionContent extends Content {
        private final String major;
        private final String minor;
        private final String patch;

        public VersionContent(String major, String minor, String patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        public String getMajor() {
            return major;
        }

        public String getMinor() {
            return minor;
        }

        public String getPatch() {
            return patch;
        }

        @Override
        public Iterable<Object> getEntries() {
            List<Object> entries = new ArrayList<>();
            if (major != null) entries.add(major);
            if (minor != null) {
                entries.add('.');
                entries.add(minor);
            }
            if (patch != null) {
                entries.add('.');
                entries.add(patch);
            }
            return entries;
        }
    }

}
