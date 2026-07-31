package xyz.wagyourtail.commons.core;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberUtils {

    public static Number negate(Number number) {
        switch (number.getClass().getSimpleName()) {
            case "Integer":
                return -number.intValue();
            case "Long":
                return -number.longValue();
            case "Float":
                return -number.floatValue();
            case "Double":
                return -number.doubleValue();
            case "Short":
                return -number.shortValue();
            case "Byte":
                return -number.byteValue();
            case "BigInteger":
                return ((BigInteger) number).negate();
            case "BigDecimal":
                return ((BigDecimal) number).negate();
            default:
                throw new IllegalArgumentException();
        }
    }

    public static String toHex(byte[] hexBytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : hexBytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    public static int nextPowerOf2(int num) {
        return Integer.highestOneBit(num - 1) << 1;
    }

    public static long nextPowerOf2(long num) {
        return Long.highestOneBit(num - 1) << 1;
    }

}
