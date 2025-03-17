package xyz.wagyourtail.commons.core.function;

import java.io.IOException;

public interface IOFunction<T, R> {
    R apply(T t) throws IOException;

}
