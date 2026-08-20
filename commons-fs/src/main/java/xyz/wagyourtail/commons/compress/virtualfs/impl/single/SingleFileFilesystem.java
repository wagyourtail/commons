package xyz.wagyourtail.commons.compress.virtualfs.impl.single;

import lombok.val;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;
import xyz.wagyourtail.commons.compress.virtualfs.VirtualFileSystem;

import java.io.IOException;
import java.util.Map;

public abstract class SingleFileFilesystem extends VirtualFileSystem {

    public SingleFileFilesystem(VirtualFile fi) {
        super(fi);
    }

    @Override
    protected Map<String, VirtualFile> resolveFiles() {
        String name;
        int last = this.getNormalizedFileName().lastIndexOf('.');
        if (last == -1) {
            name = this.getNormalizedFileName();
        } else {
            name = this.getNormalizedFileName().substring(0, last);
        }
        VirtualFile fi = new VirtualFile(this, name);
        return Map.of(name, fi);
    }

    @Override
    public long getSize(VirtualFile fi) throws IOException {
        if (this.getFiles().contains(fi)) {
            val data = fi.getData();
            if (data == null) {
                return -1;
            }
            return data.size();
        }
        return -1;
    }

    @Override
    public long getCompressedSize(VirtualFile fi) throws IOException {
        if (this.getFiles().contains(fi)) {
            return location.getSize();
        }
        return -1;
    }

}
