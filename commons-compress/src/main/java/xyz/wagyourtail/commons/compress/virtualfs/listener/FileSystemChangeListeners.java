package xyz.wagyourtail.commons.compress.virtualfs.listener;

import xyz.wagyourtail.commons.compress.virtualfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

public class FileSystemChangeListeners implements FileSystemChangeListener {

    private final List<FileSystemChangeListener> listeners = new ArrayList<>();

    public void addListener(FileSystemChangeListener listener) {
        this.listeners.add(listener);
    }

    public boolean removeListener(FileSystemChangeListener listener) {
        return this.listeners.remove(listener);
    }

    public void clearListeners() {
        this.listeners.clear();
    }

    @Override
    public void onAdded(VirtualFile file) {
        for (FileSystemChangeListener listener : this.listeners) {
            listener.onAdded(file);
        }
    }

    @Override
    public void onRemoved(VirtualFile file) {
        for (FileSystemChangeListener listener : this.listeners) {
            listener.onRemoved(file);
        }
    }

    @Override
    public void onModified(VirtualFile file) {
        for (FileSystemChangeListener listener : this.listeners) {
            listener.onModified(file);
        }
    }

}
