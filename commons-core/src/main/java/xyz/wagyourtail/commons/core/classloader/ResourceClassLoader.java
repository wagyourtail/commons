package xyz.wagyourtail.commons.core.classloader;

import lombok.SneakyThrows;
import lombok.val;
import xyz.wagyourtail.commons.core.IOUtils;
import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.classloader.provider.ClassLoaderResourceProvider;
import xyz.wagyourtail.commons.core.classloader.provider.JarFileResourceProvider;
import xyz.wagyourtail.commons.core.collection.FlatMapEnumeration;
import xyz.wagyourtail.commons.core.collection.MapEnumeration;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;

public class ResourceClassLoader extends ClassLoader implements Closeable {
    private final List<String> multiVersionList = new ArrayList<>();
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
        // noinspection deprecation
        addDelegate(new ClassLoaderResourceProvider(new URLClassLoader(failed.toArray(new URL[0]))));
    }

    public void addDelegate(ResourceProvider resourceProvider) {
        delegates.add(resourceProvider);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String internalName = name.replace('.', '/');
        String path = internalName + ".class";
        try {
            Map.Entry<ResourceProvider, URL> providerAndUrl = findResourceProviderFor(path).nextElement();
            ResourceProvider provider = providerAndUrl.getKey();
            URL resource = providerAndUrl.getValue();
            if (resource == null) {
                return super.findClass(name);
            }
            int i = internalName.lastIndexOf('/');
            if (i != -1) {
                val packageName = internalName.substring(0, i);
                val info = provider.getPackageInfo(packageName);
                if (info != null) {
                    try {
                        definePackage(
                                packageName.replace('/', '.'),
                                info.getSpecTitle(),
                                info.getSpecVersion(),
                                info.getSpecVendor(),
                                info.getImplTitle(),
                                info.getImplVersion(),
                                info.getImplVendor(),
                                null
                        );
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            try (InputStream is = resource.openStream()) {
                return transformClass(name, IOUtils.readAllBytes(is));
            }
        } catch (IOException | NullPointerException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    protected Class<?> transformClass(String name, byte[] classBytes) throws ClassNotFoundException {
        return defineClass(name, classBytes, 0, classBytes.length);
    }

    protected int maxClassVersionSupported() {
        return Utils.getCurrentClassVersion();
    }

    protected List<String> multiVersionPrefixes() {
        if (multiVersionList.isEmpty()) {
            synchronized (multiVersionList) {
                if (multiVersionList.isEmpty()) {
                    for (int i = maxClassVersionSupported(); i >= 51; --i) {
                        if (i == 51) {
                            multiVersionList.add("");
                        } else {
                            multiVersionList.add("META-INF/versions/" + Utils.classVersionToMajorVersion(i) + "/");
                        }
                    }
                }
            }
        }
        return multiVersionList;
    }

    @Override
    @SneakyThrows
    protected URL findResource(String name) {
        Enumeration<URL> urls = findResources(name);
        return urls.hasMoreElements() ? urls.nextElement() : null;
    }

    protected Enumeration<Map.Entry<ResourceProvider, URL>> findResourceProviderFor(final String name) throws IOException {
        return new FlatMapEnumeration<ResourceProvider, Map.Entry<ResourceProvider, URL>>(Collections.enumeration(delegates)) {

            @Override
            protected Enumeration<Map.Entry<ResourceProvider, URL>> mapper(final ResourceProvider provider) {
                return new FlatMapEnumeration<String, Map.Entry<ResourceProvider, URL>>(Collections.enumeration(name.startsWith("META-INF/versions/") ? Collections.singleton("") : multiVersionPrefixes())) {

                    @Override
                    @SneakyThrows
                    protected Enumeration<Map.Entry<ResourceProvider, URL>> mapper(String prefix) {
                        return new MapEnumeration<URL, Map.Entry<ResourceProvider, URL>>(provider.getResources(prefix + name)) {

                            @Override
                            protected Map.Entry<ResourceProvider, URL> mapper(URL element) {
                                return new AbstractMap.SimpleImmutableEntry<>(provider, element);
                            }
                        };
                    }
                };
            }
        };
    }

    @Override
    protected Enumeration<URL> findResources(final String name) throws IOException {
        return new MapEnumeration<Map.Entry<ResourceProvider, URL>, URL>(findResourceProviderFor(name)) {
            @Override
            protected URL mapper(Map.Entry<ResourceProvider, URL> element) {
                return element.getValue();
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
