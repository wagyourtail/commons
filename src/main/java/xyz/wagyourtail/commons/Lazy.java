package xyz.wagyourtail.commons;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    private volatile boolean initialized = false;
    private T value;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (!this.initialized) {
            synchronized (this) {
                if (!this.initialized) {
                    this.value = this.supplier.get();
                    this.initialized = true;
                }
            }
        }
        return this.value;
    }

}
