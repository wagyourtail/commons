package xyz.wagyourtail.commons.compress.virtualfs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

public abstract class VirtualFileSystemFactory<T extends VirtualFileSystem> {

    private static final Map<String, VirtualFileSystemFactory<?>> providers = new HashMap<>();

    static {
        for (VirtualFileSystemFactory<?> provider : ServiceLoader.load(VirtualFileSystemFactory.class)) {
            for (String mime : provider.getValidMimes()) {
                providers.put(mime, provider);
            }
        }
    }

    public static Set<String> getSupportedMimes() {
        return providers.keySet();
    }

    public static VirtualFileSystemFactory<?> getProvider(String mime) {
        return providers.get(mime);
    }

    public abstract String[] getValidMimes();

    public abstract T read(VirtualFile fi) throws IOException;

    public abstract T create(String fileName) throws IOException;

}
