package xyz.wagyourtail.commons.core.classloader.provider;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.wagyourtail.commons.core.io.SeekableInMemoryByteChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PackedResourceProviderTest {

    public byte[] createPackedResources() throws IOException {
        val os = new ByteArrayOutputStream();
        val out = new PackedResourceProvider.PackedResourceOutputStream(os);
        out.putNextEntry("test");
        out.write(new byte[] {1, 2, 3});
        out.closeEntry();
        out.putNextEntry("test2");
        out.write(new byte[] {4, 5, 6});
        out.closeEntry();
        out.close();
        return os.toByteArray();
    }

    @Test
    public void test() throws IOException {
        val provider = new PackedResourceProvider(new SeekableInMemoryByteChannel(createPackedResources()));
        val test1 = provider.getResources("test").nextElement().openStream().readAllBytes();
        val test2 = provider.getResources("test2").nextElement().openStream().readAllBytes();
        assertArrayEquals(new byte[] {1, 2, 3}, test1);
        assertArrayEquals(new byte[] {4, 5, 6}, test2);
    }

}