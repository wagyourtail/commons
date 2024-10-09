package xyz.wagyourtail.commons.gradle.filter

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import java.io.ByteArrayInputStream
import java.io.InputStream

class PackageRelocator(val map: Map<String, String>) : Remapper(), ContentMapper.StreamMapper {

    override fun map(internalName: String): String {
        for ((from, to) in map) {
            if (internalName.startsWith(from)) {
                return to + internalName.substring(from.length)
            }
        }
        return internalName
    }

    override fun map(input: InputStream): InputStream {
        val reader = ClassReader(input)
        val writer = ClassWriter(0)
        reader.accept(ClassRemapper(writer, this), 0)
        return ByteArrayInputStream(writer.toByteArray())
    }

}
