package xyz.wagyourtail.commons.parsers.impl.constant.test;

import org.junit.jupiter.api.Test;
import xyz.wagyourtail.commons.core.reader.ParseException;
import xyz.wagyourtail.commons.parsers.StringData;
import xyz.wagyourtail.commons.parsers.impl.Constant;

import static org.junit.jupiter.api.Assertions.*;

public class ConstantTest {

    @Test
    public void basicTests() {
        Constant.parse("null");
        Constant.parse("false");
        Constant.parse("100");
        Constant.parse("10e+24");
        Constant.parse("100.23e-46");
        Constant.parse(".23e-46");
        Constant.parse("0xFFFF10");
        Constant.parse("-100");
        Constant.parse("-InfinityF");
        Constant.parse("-.2f");
        Constant.parse("\"string\"");
    }

    @Test
    public void testIllegal() {
        assertThrows(ParseException.class, () -> Constant.parse("09"));

        assertThrows(ParseException.class, () -> Constant.parse("0xg"));

        assertThrows(ParseException.class, () -> Constant.parse("0b"));

        assertThrows(ParseException.class, () -> Constant.parse("0b2"));

        assertThrows(ParseException.class, () -> Constant.parse("0x"));

        assertThrows(ParseException.class, () -> Constant.parse("0x1h"));
    }

    @Test
    public void testVisitor() {
        assertEquals("null", StringData.BuildStringVisitor.apply(Constant.parse("null")));
        assertEquals("true", StringData.BuildStringVisitor.apply(Constant.parse("true")));
        assertEquals("10e+24", StringData.BuildStringVisitor.apply(Constant.parse("10e+24")));
        assertEquals("-NaND", StringData.BuildStringVisitor.apply(Constant.parse("-NaND")));
        assertEquals("0.10e+24f", StringData.BuildStringVisitor.apply(Constant.parse("0.10e+24f")));
        assertEquals("0b1010", StringData.BuildStringVisitor.apply(Constant.parse("0b1010")));
        assertEquals("0x1010L", StringData.BuildStringVisitor.apply(Constant.parse("0x1010L")));
        assertEquals("0123l", StringData.BuildStringVisitor.apply(Constant.parse("0123l")));
        assertEquals("0", StringData.BuildStringVisitor.apply(Constant.parse("0")));
        assertEquals("1", StringData.BuildStringVisitor.apply(Constant.parse("1")));
    }

    @Test
    public void testValues() {
        assertNull(Constant.parse("null").getValue());
        assertEquals(true, Constant.parse("true").getValue());
        assertEquals(10e24, Constant.parse("10e+24").getValue());
        assertEquals(Double.NaN, Constant.parse("NaND").getValue());
        assertEquals(0.10e24f, Constant.parse("0.10e+24f").getValue());
        assertEquals(0b1010, Constant.parse("0b1010").getValue());
        assertEquals(0x1010L, Constant.parse("0x1010L").getValue());
        assertEquals(0123l, Constant.parse("0123l").getValue());
        assertEquals(0, Constant.parse("0").getValue());
        assertEquals(1, Constant.parse("1").getValue());
        assertEquals(-110, Constant.parse("-110").getValue());
        assertEquals(-0x109L, Constant.parse("-0x109L").getValue());
        assertEquals("Test String", Constant.parse("\"Test String\"").getValue());
    }

}
