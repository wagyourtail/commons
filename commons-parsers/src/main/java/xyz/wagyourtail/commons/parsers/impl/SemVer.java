package xyz.wagyourtail.commons.parsers.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;
import xyz.wagyourtail.commons.parsers.impl.semver.Build;
import xyz.wagyourtail.commons.parsers.impl.semver.PreRelease;
import xyz.wagyourtail.commons.parsers.impl.semver.VersionCore;

import java.util.ArrayList;
import java.util.List;

public class SemVer extends StringData.OnlyParsed<SemVer.SemVerContent> implements Comparable<SemVer> {


    public SemVer(SemVerContent content) {
        super(content);
    }


    public static SemVer parse(String rawContent) {
        StringCharReader reader = new StringCharReader(rawContent);
        val content = contentBuilder(reader);
        reader.expectEOS();
        return new SemVer(content);
    }

    public static SemVer parse(CharReader<?> reader) {
        return new SemVer(contentBuilder(reader));
    }

    private static SemVerContent contentBuilder(CharReader<?> reader) {
        val core = VersionCore.parse(reader);
        PreRelease preRelease = null;
        if (reader.peek() == '-') {
            reader.take();
            preRelease = PreRelease.parse(reader);
        }
        Build build = null;
        if (reader.peek() == '+') {
            reader.take();
            build = Build.parse(reader);
        }
        return new SemVerContent(core, preRelease, build);
    }

    @Override
    public int compareTo(@NonNull SemVer other) {
        int c = getContent().core.compareTo(other.getContent().core);
        if (c != 0) return c;
        if (getContent().preRelease != null ^ other.getContent().preRelease != null) {
            return getContent().preRelease == null ? -1 : 1;
        }
        if (getContent().preRelease != null) {
            return getContent().preRelease.compareTo(other.getContent().preRelease);
        }
        return 0;
    }

    @AllArgsConstructor
    @Getter
    public static class SemVerContent extends Data.Content<Object> {
        private final VersionCore core;
        private final PreRelease preRelease;
        private final Build build;

        @Override
        public Iterable<Object> getEntries() {
            List<Object> entries = new ArrayList<>();
            entries.add(core);
            if (preRelease != null) entries.add(preRelease);
            if (build != null) entries.add(build);
            return entries;
        }
    }

}
