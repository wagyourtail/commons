package xyz.wagyourtail.commons.core.classloader.provider;

import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.classloader.ResourceProvider;
import xyz.wagyourtail.commons.core.function.IOSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileResourceProvider implements ResourceProvider {
    private final JarFile jarFile;

    public JarFileResourceProvider(JarFile jarFile) {
        this.jarFile = jarFile;
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
