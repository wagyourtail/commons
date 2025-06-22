package xyz.wagyourtail.commons.core;

import java.io.IOException;
import java.nio.*;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SeekableByteChannelUtils {

    public static byte readByte(SeekableByteChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        channel.read(buffer);
        buffer.flip();
        return buffer.get();
    }

    public static short readShort(SeekableByteChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Short.SIZE / 8);
        channel.read(buffer);
        buffer.flip();
        return buffer.getShort();
    }

    public static int readInt(SeekableByteChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / 8);
        channel.read(buffer);
        buffer.flip();
        return buffer.getInt();
    }

    public static long readLong(SeekableByteChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / 8);
        channel.read(buffer);
        buffer.flip();
        return buffer.getLong();
    }

    public static float readFloat(SeekableByteChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Float.SIZE / 8);
        channel.read(buffer);
        buffer.flip();
        return buffer.getFloat();
    }

    public static double readDouble(SeekableByteChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Double.SIZE / 8);
        channel.read(buffer);
        buffer.flip();
        return buffer.getDouble();
    }

    public static byte[] readBytes(SeekableByteChannel channel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        channel.read(buffer);
        buffer.flip();
        return buffer.array();
    }

    public static short[] readShorts(SeekableByteChannel channel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate((Short.SIZE / 8) * length);
        channel.read(buffer);
        buffer.flip();
        short[] shorts = new short[length];
        ShortBuffer shortBuffer = buffer.asShortBuffer();
        shortBuffer.get(shorts);
        return shorts;
    }

    public static int[] readInts(SeekableByteChannel channel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate((Integer.SIZE / 8) * length);
        channel.read(buffer);
        buffer.flip();
        int[] ints = new int[length];
        IntBuffer intBuffer = buffer.asIntBuffer();
        intBuffer.get(ints);
        return ints;
    }

    public static long[] readLongs(SeekableByteChannel channel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate((Long.SIZE / 8) * length);
        channel.read(buffer);
        buffer.flip();
        long[] longs = new long[length];
        LongBuffer longBuffer = buffer.asLongBuffer();
        longBuffer.get(longs);
        return longs;
    }

    public static float[] readFloats(SeekableByteChannel channel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate((Float.SIZE / 8) * length);
        channel.read(buffer);
        buffer.flip();
        float[] floats = new float[length];
        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        floatBuffer.get(floats);
        return floats;
    }

    public static double[] readDoubles(SeekableByteChannel channel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate((Double.SIZE / 8) * length);
        channel.read(buffer);
        buffer.flip();
        double[] doubles = new double[length];
        DoubleBuffer doubleBuffer = buffer.asDoubleBuffer();
        doubleBuffer.get(doubles);
        return doubles;
    }

    public static String readString(SeekableByteChannel channel, int byteLength) throws IOException {
        byte[] bytes = readBytes(channel, byteLength);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String readString(SeekableByteChannel channel, int length, Charset charset) throws IOException {
        byte[] bytes = readBytes(channel, length);
        return new String(bytes, charset);
    }

}
