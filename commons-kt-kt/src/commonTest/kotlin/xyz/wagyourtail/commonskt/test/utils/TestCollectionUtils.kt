package xyz.wagyourtail.commonskt.test.utils

import xyz.wagyourtail.commonskt.utils.resolveArgs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestCollectionUtils {

    @Test
    fun testResolveArgs() {
        val map = mapOf(
            "input" to "example.jar",
            "output" to "output.jar",
            "wasd" to "qwer"
        )

        val args = listOf(
            "{input}",
            "--output={output}",
            "{wasd}",
            "nothing"
        )

        val expected = listOf(
            "example.jar",
            "--output=output.jar",
            "qwer",
            "nothing"
        )

        assertEquals(expected, map.resolveArgs(args, false))
    }

    @Test
    fun testResolveArgsWithDollar() {
        val map = mapOf(
            "input" to "example.jar",
            "output" to "output.jar",
            "wasd" to "qwer"
        )

        val args = listOf(
            "\${input}",
            "--output=\${output}",
            "\${wasd}",
            "nothing"
        )

        val expected = listOf(
            "example.jar",
            "--output=output.jar",
            "qwer",
            "nothing"
        )

        assertEquals(expected, map.resolveArgs(args, true))
    }

    @Test
    fun testMissingArg() {
        val map = mapOf(
            "input" to "example.jar",
            "output" to "output.jar",
        )

        val args = listOf(
            "\${input}",
            "--output=\${output}",
            "\${wasd}",
            "nothing"
        )

        assertFailsWith<IllegalArgumentException>("Property wasd not found") {
            map.resolveArgs(args, true)
        }
    }

}