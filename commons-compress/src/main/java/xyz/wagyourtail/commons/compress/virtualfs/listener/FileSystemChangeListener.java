package xyz.wagyourtail.commons.compress.virtualfs.listener;

import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;

public interface FileSystemChangeListener {

    void onAdded(VirtualFile file);

    void onRemoved(VirtualFile file);

    void onModified(VirtualFile file);

}
