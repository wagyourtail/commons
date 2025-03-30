package xyz.wagyourtail.commonskt.test.maven

import xyz.wagyourtail.commonskt.maven.MavenCoords
import kotlin.test.Test
import kotlin.test.assertEquals

class MavenCoordsTest {

    @Test
    fun test() {
        val first = MavenCoords("group.name", "artifact", "1.0.0-SNAPSHOT", "test")
        assertEquals("group.name", first.group)
        assertEquals("artifact", first.artifact)
        assertEquals("1.0.0-SNAPSHOT", first.version)
        assertEquals("test", first.classifier)
        assertEquals("jar", first.extension)

        val second = MavenCoords("group.name", "artifact", "1.0.0-SNAPSHOT")
        assertEquals("group.name", second.group)
        assertEquals("artifact", second.artifact)
        assertEquals("1.0.0-SNAPSHOT", second.version)
        assertEquals(null, second.classifier)
        assertEquals("jar", second.extension)

        val third = MavenCoords("group.name", "artifact")
        assertEquals("group.name", third.group)
        assertEquals("artifact", third.artifact)
        assertEquals(null, third.version)
        assertEquals(null, third.classifier)
        assertEquals("jar", third.extension)

        val fourth = MavenCoords("artifact")
        assertEquals(null, fourth.group)
        assertEquals("artifact", fourth.artifact)
        assertEquals(null, fourth.version)
        assertEquals(null, fourth.classifier)
        assertEquals("jar", fourth.extension)

        val fifth = MavenCoords("group.name", "artifact", "1.0.0-SNAPSHOT", extension = "zip")
        assertEquals("group.name", fifth.group)
        assertEquals("artifact", fifth.artifact)
        assertEquals("1.0.0-SNAPSHOT", fifth.version)
        assertEquals(null, fifth.classifier)
        assertEquals("zip", fifth.extension)

        val sixth = MavenCoords("group.name", "artifact", extension = "zip")
        assertEquals("group.name", sixth.group)
        assertEquals("artifact", sixth.artifact)
        assertEquals(null, sixth.version)
        assertEquals(null, sixth.classifier)
        assertEquals("zip", sixth.extension)
    }

}