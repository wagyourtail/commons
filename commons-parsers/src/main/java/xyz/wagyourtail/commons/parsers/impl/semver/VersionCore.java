package xyz.wagyourtail.commons.parsers.impl.semver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import xyz.wagyourtail.commons.core.reader.CharReader;
import xyz.wagyourtail.commons.core.reader.StringCharReader;
import xyz.wagyourtail.commons.parsers.Data;
import xyz.wagyourtail.commons.parsers.StringData;

import java.util.Arrays;

public class VersionCore extends StringData.OnlyParsed<VersionCore.VersionCoreContent> implements Comparable<VersionCore> {

    public VersionCore(VersionCoreContent content) {
        super(content);
    }

    public static VersionCore parse(String raw) {
        StringCharReader reader = new StringCharReader(raw);
        val content = contentBuilder(reader);
        reader.expectEOS();
        return new VersionCore(content);
    }

    public static VersionCore parse(CharReader<?> reader) {
        return new VersionCore(contentBuilder(reader));
    }

    public static VersionCoreContent contentBuilder(CharReader<?> reader) {
        long major = reader.takeWholeNumber();
        reader.expect('.');
        long minor = reader.takeWholeNumber();
        reader.expect('.');
        long patch = reader.takeWholeNumber();
        return new VersionCoreContent(major, minor, patch);
    }

    @Override
    public int compareTo(VersionCore o) {
        int c = Long.compare(getContent().major, o.getContent().major);
        if (c != 0) return c;
        c = Long.compare(getContent().minor, o.getContent().minor);
        if (c != 0) return c;
        return Long.compare(getContent().patch, o.getContent().patch);
    }

    @Getter
    @AllArgsConstructor
    public static class VersionCoreContent extends Data.Content<Object> {
        private final long major;
        private final long minor;
        private final long patch;

        @Override
        public Iterable<Object> getEntries() {
            return Arrays.asList(major, '.', minor, '.', patch);
        }


    }

}
