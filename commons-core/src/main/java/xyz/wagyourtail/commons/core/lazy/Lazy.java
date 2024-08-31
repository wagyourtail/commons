package xyz.wagyourtail.commons.core.lazy;

public abstract class Lazy<T> {

    private volatile boolean initialized = false;
    private T value;

    public Lazy() {
    }

    public final T get() {
        if (!this.initialized) {
            synchronized (this) {
                if (!this.initialized) {
                    this.value = supplier();
                    this.initialized = true;
                }
            }
        }
        return this.value;
    }

    protected abstract T supplier();

}
