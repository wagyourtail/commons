package xyz.wagyourtail.commons.core.function;

import java.io.IOException;

public interface IOSupplier<T> {
    T get() throws IOException;
}
