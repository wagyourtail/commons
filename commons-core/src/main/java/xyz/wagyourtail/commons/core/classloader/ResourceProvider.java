package xyz.wagyourtail.commons.core.classloader;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public abstract class ResourceProvider implements Closeable {

    public PackageInfo getPackageInfo(String name) throws IOException {
        return null;
    }

    public abstract Enumeration<URL> getResources(String name) throws IOException;

    @Builder
    @Getter
    public static class PackageInfo {
        @Builder.Default
        private final String specTitle = null;
        @Builder.Default
        private final String specVersion = null;
        @Builder.Default
        private final String specVendor = null;
        @Builder.Default
        private final String implTitle = null;
        @Builder.Default
        private final String implVersion = null;
        @Builder.Default
        private final String implVendor = null;
    }

}
