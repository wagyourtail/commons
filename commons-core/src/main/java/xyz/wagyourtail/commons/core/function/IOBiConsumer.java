package xyz.wagyourtail.commons.core.function;

import java.io.IOException;

public interface IOBiConsumer<T, U> {

    void accept(T t, U u) throws IOException;

}
