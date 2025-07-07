package xyz.wagyourtail.commons.core.classloader;

import lombok.*;
import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.lazy.Lazy;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public abstract class ResourceProvider implements Closeable {

    private final Lazy<PackageInfo> packageInfo = new Lazy<PackageInfo>() {

        @Override
        @SneakyThrows
        protected PackageInfo supplier() {
            val manifestURLs =  getResources("META-INF/MANIFEST.MF");
            if (!manifestURLs.hasMoreElements()) {
                return null;
            }
            val manifestURL = manifestURLs.nextElement();
            val data = Utils.readAllBytes(manifestURL.openStream());
            Manifest manifestFile = new Manifest();
            manifestFile.read(new ByteArrayInputStream(data));
            Attributes mainAttributes = manifestFile.getMainAttributes();
            return PackageInfo.builder()
                    .specTitle(mainAttributes.getValue("Specification-Title"))
                    .specVersion(mainAttributes.getValue("Specification-Version"))
                    .specVendor(mainAttributes.getValue("Specification-Vendor"))
                    .implTitle(mainAttributes.getValue("Implementation-Title"))
                    .implVersion(mainAttributes.getValue("Implementation-Version"))
                    .implVendor(mainAttributes.getValue("Implementation-Vendor"))
                    .build();
        }

    };

    public PackageInfo getPackageInfo(String name) throws IOException {
        return packageInfo.get();
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
