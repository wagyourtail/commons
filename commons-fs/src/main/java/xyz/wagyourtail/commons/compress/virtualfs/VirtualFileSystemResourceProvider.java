package xyz.wagyourtail.commons.compress.virtualfs;

import lombok.val;
import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.classloader.ResourceProvider;
import xyz.wagyourtail.commons.core.io.SeekableByteChannelInputStream;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

public class VirtualFileSystemResourceProvider extends ResourceProvider {
    private final VirtualFileSystem vfs;

    public VirtualFileSystemResourceProvider(VirtualFileSystem vfs) {
        this.vfs = vfs;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        VirtualFile vf = vfs.getFile(name);
        if (!vf.exists()) return Collections.emptyEnumeration();
        return Collections.enumeration(Collections.singleton(Utils.bufferURL(name, () -> {
            val data = vf.getData();
            if (data == null) return null;
            return new SeekableByteChannelInputStream(data);
        })));
    }

    @Override
    public void close() throws IOException {
        this.vfs.close();
    }

}
