package xyz.wagyourtail.commons.parsers.impl.semver.range;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import lombok.var;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.ArrayList;
import java.util.List;

public class ExactVersionRange extends RangeData<ExactVersionRange.VersionContent> {

    public ExactVersionRange(VersionContent content) {
        super(content);
    }

    public static ExactVersionRange parse(String raw) {
        StringCharReader reader = new StringCharReader(raw);
        val content = contentBuilder(reader);
        reader.expectEOS();
        return new ExactVersionRange(content);
    }

    public static ExactVersionRange parse(CharReader<?> reader) {
        return new ExactVersionRange(contentBuilder(reader));
    }

    public static ExactVersionRange.VersionContent contentBuilder(CharReader<?> reader) {
        val major = reader.parse(
                e -> e.takeWholeNumber(false),
                e -> e.expect("wildcard", c -> c == '*' || c == 'x' || c == 'X')
        );
        if (reader.peek() != '.') return new VersionContent(String.valueOf(major), null, null);
        else reader.take();
        val minor = reader.parse(
                e -> e.takeWholeNumber(false),
                e -> e.expect("wildcard", c -> c == '*' || c == 'x' || c == 'X')
        );
        if (reader.peek() != '.') return new VersionContent(String.valueOf(major), String.valueOf(minor), null);
        else reader.take();
        val patch = reader.parse(
                e -> e.takeWholeNumber(false),
                e -> e.expect("wildcard", c -> c == '*' || c == 'x' || c == 'X')
        );
        return new VersionContent(String.valueOf(major), String.valueOf(minor), String.valueOf(patch));
    }

    @Override
    public boolean contains(SemVer version) {
        return compare(version) == 0;
    }

    public boolean equals(SemVer version) {
        return contains(version);
    }

    public int compare(SemVer version) {
        val content = getContent();
        var major = content.getMajor();

        if (major != null && (major.equals("*") || major.equalsIgnoreCase("x"))) {
            major = null;
        }

        var minor = content.getMinor();

        if (minor != null && (minor.equals("*") || minor.equalsIgnoreCase("x"))) {
            minor = null;
        }

        var patch = content.getPatch();

        if (patch != null && (patch.equals("*") || patch.equalsIgnoreCase("x"))) {
            patch = null;
        }

        val versionContent = version.getContent();
        val versionCore = versionContent.getCore().getContent();

        val versionMajor = versionCore.getMajor();
        if (major == null) return 0;
        val majorLong = Long.parseLong(major);
        if (versionMajor < majorLong) return -1;
        if (versionMajor > majorLong) return 1;

        if (minor == null) return 0;
        val versionMinor = versionCore.getMinor();
        val minorLong = Long.parseLong(minor);
        if (versionMinor < minorLong) return -1;
        if (versionMinor > minorLong) return 1;

        if (patch == null) return 0;
        val versionPatch = versionCore.getPatch();
        val patchLong = Long.parseLong(patch);
        val patchResult = Long.compare(patchLong, versionPatch);
        if (patchResult == 0 && versionContent.getPreRelease() != null) {
            return -1;
        }
        return patchResult;
    }

    public boolean similar(SemVer version) {
        val content = getContent();
        var major = content.getMajor();

        if (major != null && (major.equals("*") || major.equalsIgnoreCase("x"))) {
            major = null;
        }

        var minor = content.getMinor();

        if (minor != null && (minor.equals("*") || minor.equalsIgnoreCase("x"))) {
            minor = null;
        }

        var patch = content.getPatch();

        if (patch != null && (patch.equals("*") || patch.equalsIgnoreCase("x"))) {
            patch = null;
        }

        val versionContent = version.getContent();
        val versionCore = versionContent.getCore().getContent();

        if (major == null) return true;
        val versionMajor = versionCore.getMajor();
        val majorLong = Long.parseLong(major);
        if (versionMajor != majorLong) return false;

        if (minor == null) return true;
        val versionMinor = versionCore.getMinor();
        val minorLong = Long.parseLong(minor);
        if (versionMinor != minorLong) return false;

        if (patch == null) return true;
        val versionPatch = versionCore.getPatch();
        val patchLong = Long.parseLong(patch);
        return versionPatch >= patchLong;
    }

    public boolean compatible(SemVer version) {
        val content = getContent();
        var major = content.getMajor();

        if (major != null && (major.equals("*") || major.equalsIgnoreCase("x"))) {
            major = null;
        }

        var minor = content.getMinor();

        if (minor != null && (minor.equals("*") || minor.equalsIgnoreCase("x"))) {
            minor = null;
        }

        var patch = content.getPatch();

        if (patch != null && (patch.equals("*") || patch.equalsIgnoreCase("x"))) {
            patch = null;
        }

        val versionContent = version.getContent();
        val versionCore = versionContent.getCore().getContent();

        if (major == null) return true;
        val versionMajor = versionCore.getMajor();
        val majorLong = Long.parseLong(major);
        if (versionMajor != majorLong) return false;

        if (minor == null) return true;
        val versionMinor = versionCore.getMinor();
        val minorLong = Long.parseLong(minor);
        if (majorLong == 0 && versionMinor != minorLong) return false;
        if (versionMinor < minorLong) return false;
        if (versionMinor > minorLong) return true;

        if (patch == null) return true;
        val versionPatch = versionCore.getPatch();
        val patchLong = Long.parseLong(patch);
        if (minorLong == 0 && versionPatch != patchLong) return false;
        return versionPatch >= patchLong;
    }

    @Getter
    @AllArgsConstructor
    public static class VersionContent extends Data.Content<Object> {
        private final String major;
        private final String minor;
        private final String patch;

        @Override
        public Iterable<Object> getEntries() {
            List<Object> entries = new ArrayList<>();
            entries.add(major);
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
