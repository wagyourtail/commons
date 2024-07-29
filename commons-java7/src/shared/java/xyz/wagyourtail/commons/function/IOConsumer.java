package xyz.wagyourtail.commons.function;

import java.io.IOException;

public interface IOConsumer<T> {
    void accept(T t) throws IOException;
}
