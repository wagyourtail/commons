package xyz.wagyourtail.commons.core.classloader.provider;

import xyz.wagyourtail.commons.core.classloader.ResourceProvider;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

@Deprecated
public class ClassLoaderResourceProvider extends ResourceProvider {
    private final ClassLoader classLoader;

    public ClassLoaderResourceProvider(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public PackageInfo getPackageInfo(String name) throws IOException {
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return classLoader.getResources(name);
    }

    @Override
    public void close() throws IOException {
        if (classLoader instanceof Closeable) {
            ((Closeable) classLoader).close();
        } else if (classLoader instanceof AutoCloseable) {
            try {
                ((AutoCloseable) classLoader).close();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

}
