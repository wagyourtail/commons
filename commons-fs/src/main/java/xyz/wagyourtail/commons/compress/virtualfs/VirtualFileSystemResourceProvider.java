package xyz.wagyourtail.commons.compress.virtualfs;

import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.classloader.ResourceProvider;
import xyz.wagyourtail.commons.core.io.SeekableByteChannelInputStream;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

public class VirtualFileSystemResourceProvider implements ResourceProvider {
    private final VirtualFileSystem vfs;

    public VirtualFileSystemResourceProvider(VirtualFileSystem vfs) {
        this.vfs = vfs;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return Collections.enumeration(Collections.singleton(Utils.bufferURL(name, () ->
            new SeekableByteChannelInputStream(vfs.getFile(name).getData())
        )));
    }

    @Override
    public void close() throws IOException {
        this.vfs.close();
    }

}
