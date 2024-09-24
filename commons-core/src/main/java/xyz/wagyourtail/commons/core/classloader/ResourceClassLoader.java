package xyz.wagyourtail.commons.core.classloader;

import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.classloader.provider.ClassLoaderResourceProvider;
import xyz.wagyourtail.commons.core.classloader.provider.JarFileResourceProvider;
import xyz.wagyourtail.commons.core.collection.FlatMapEnumeration;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

public class ResourceClassLoader extends ClassLoader implements Closeable {
    private final List<ResourceProvider> delegates = new ArrayList<>();

    public ResourceClassLoader(ClassLoader parent) {
        super(parent);
    }

    public ResourceClassLoader(List<ResourceProvider> resources, ClassLoader parent) {
        super(parent);
        this.delegates.addAll(resources);
    }

    public ResourceClassLoader(Set<URL> urls, ClassLoader parent) {
        super(parent);
        Set<URL> failed = new HashSet<>();
        for (URL url : urls) {
            try {
                addDelegate(new JarFileResourceProvider(new JarFile(Paths.get(url.toURI()).toFile())));
            } catch (Exception e) {
                failed.add(url);
            }
        }
        // fallback on normal classloader
        addDelegate(new ClassLoaderResourceProvider(new URLClassLoader(failed.toArray(new URL[0]))));
    }

    public void addDelegate(ResourceProvider resourceProvider) {
        delegates.add(resourceProvider);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String internalName = name.replace('.', '/');
        String path = internalName + ".class";
        URL resource = findResource(path);
        if (resource == null) {
            return super.findClass(name);
        }
        try (InputStream is = resource.openStream()) {
            return transformClass(name, Utils.readAllBytes(is));
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    protected Class<?> transformClass(String name, byte[] classBytes) throws ClassNotFoundException {
        return defineClass(name, classBytes, 0, classBytes.length);
    }

    @Override
    protected URL findResource(String name) {
        return findResources(name).nextElement();
    }

    @Override
    protected Enumeration<URL> findResources(final String name) {
        return new FlatMapEnumeration<ResourceProvider, URL>(Collections.enumeration(delegates)) {
            @Override
            protected Enumeration<URL> mapper(ResourceProvider element) {
                try {
                    return element.getResources(name);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public void close() throws IOException {
        for (ResourceProvider delegate : delegates) {
            delegate.close();
        }
    }
}
