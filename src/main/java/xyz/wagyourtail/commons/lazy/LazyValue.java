package xyz.wagyourtail.commons.lazy;

import lombok.val;
import xyz.wagyourtail.commons.core.lazy.Lazy;

import java.util.function.Supplier;

public class LazyValue<T> extends Lazy<T> {

    public Supplier<T> supplier;

    public LazyValue(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected T supplier() {
        val value = supplier.get();
        supplier = null;
        return value;
    }

}
