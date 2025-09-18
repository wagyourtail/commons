package xyz.wagyourtail.commons.java8.test.utils;

import org.junit.jupiter.api.Test;
import xyz.wagyourtail.commons.java8.utils.IteratorUtils8;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IteratorUtilsTest {

    @Test
    public void testZip() {
        var list1 = List.of(1, 3, 5);
        var list2 = List.of(2, 4, 6);

        var result = IteratorUtils8.zip(list1.iterator(), list2.iterator());
        assertEquals(List.of(1, 2, 3, 4, 5, 6), IteratorUtils8.toList(result));
    }

}
