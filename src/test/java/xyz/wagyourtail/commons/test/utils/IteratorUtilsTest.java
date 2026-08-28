package xyz.wagyourtail.commons.test.utils;

import org.junit.jupiter.api.Test;
import xyz.wagyourtail.commons.utils.IteratorUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IteratorUtilsTest {

    @Test
    public void testZip() {
        var list1 = List.of(1, 3, 5);
        var list2 = List.of(2, 4, 6);

        var result = IteratorUtils.zip(list1.iterator(), list2.iterator());
        assertEquals(List.of(1, 2, 3, 4, 5, 6), IteratorUtils.toList(result));
    }

    @Test
    public void testFilter() {
        var list = List.of(1, 2, 3, 4, 5, 6, 7);
        var result = IteratorUtils.filter(list.iterator(), x -> x % 2 == 0);
        assertEquals(List.of(2, 4, 6), IteratorUtils.toList(result));
    }

}
