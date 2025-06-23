package xyz.wagyourtail.commons.core;

import java.util.Arrays;

public class ArrayUtils {

    public static <T> T[] trimOrOriginal(T[] array, int length) {
        if (array.length == length) {
            return array;
        } else {
            return Arrays.copyOf(array, length);
        }
    }

    public static byte[] trimOrOriginal(byte[] array, int length) {
        if (array.length == length) {
            return array;
        } else {
            return Arrays.copyOf(array, length);
        }
    }

    public static char[] trimOrOriginal(char[] array, int length) {
        if (array.length == length) {
            return array;
        } else {
            return Arrays.copyOf(array, length);
        }
    }

    public static short[] trimOrOriginal(short[] array, int length) {
        if (array.length == length) {
            return array;
        } else {
            return Arrays.copyOf(array, length);
        }
    }

    public static int[] trimOrOriginal(int[] array, int length) {
        if (array.length == length) {
            return array;
        } else {
            return Arrays.copyOf(array, length);
        }
    }

    public static long[] trimOrOriginal(long[] array, int length) {
        if (array.length == length) {
            return array;
        } else {
            return Arrays.copyOf(array, length);
        }
    }

    public static float[] trimOrOriginal(float[] array, int length) {
        if (array.length == length) {
            return array;
        } else {
            return Arrays.copyOf(array, length);
        }
    }

    public static double[] trimOrOriginal(double[] array, int length) {
        if (array.length == length) {
            return array;
        } else {
            return Arrays.copyOf(array, length);
        }
    }

    public static boolean[] trimOrOriginal(boolean[] array, int length) {
        if (array.length == length) {
            return array;
        } else {
            return Arrays.copyOf(array, length);
        }
    }

}
