package xyz.wagyourtail.commons.core.classloader.provider;

import lombok.SneakyThrows;
import lombok.val;
import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.classloader.ResourceProvider;
import xyz.wagyourtail.commons.core.function.IOSupplier;
import xyz.wagyourtail.commons.core.lazy.Lazy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileResourceProvider extends ResourceProvider {
    private final JarFile jarFile;
    private final Lazy<PackageInfo> packageInfo = new Lazy<PackageInfo>() {

        @Override
        @SneakyThrows
        protected PackageInfo supplier() {
            val manifest = jarFile.getManifest();
            if (manifest == null) {
                return null;
            }
            Attributes mainAttributes = manifest.getMainAttributes();
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

    public JarFileResourceProvider(JarFile jarFile) {
        this.jarFile = jarFile;
    }


    @Override
    public PackageInfo getPackageInfo(String name) throws IOException {
        return packageInfo.get();
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        final JarEntry entry = jarFile.getJarEntry(name);
        if (entry == null) {
            return Collections.emptyEnumeration();
        }
        return Collections.enumeration(Collections.singleton(Utils.bufferURL(name, new IOSupplier<InputStream>() {

            @Override
            public InputStream get() throws IOException {
                return jarFile.getInputStream(entry);
            }

        })));
    }

    @Override
    public void close() throws IOException {
        jarFile.close();
    }

}
