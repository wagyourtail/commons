package xyz.wagyourtail.commons.function;

import java.io.IOException;

public interface IOSupplier<T> {
    T get() throws IOException;
}
