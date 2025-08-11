package xyz.wagyourtail.commons.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {

    @Test
    public void testCount() {
        assertEquals(10, StringUtils.count("aabbataaeadabaaba", 'a'));
    }

    @Test
    public void testCountRange() {
        String s = "aabbaabgaaerunfaa";
        assertEquals(4, StringUtils.count(s, 'a', 2, 11));
    }

    @Test
    public void testCountSub() {
        String s = "aabbataaeadabaaba";
        assertEquals(3, StringUtils.count(s, "aa"));
    }

    @Test
    public void testCountSubRange() {
        String s = "aabbataaeadabaaba";
        assertEquals(1, StringUtils.count(s, "aa", 2, 11));
    }

}