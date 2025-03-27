package xyz.wagyourtail.commonskt.test.matching

import xyz.wagyourtail.commonskt.matching.globToRegex
import kotlin.test.Test
import kotlin.test.assertEquals

class GlobToRegexTest {

    @Test
    fun applyTests() {
        assertEquals("^\\/?[^/]*\\.java$", "*.java".globToRegex().pattern)
        assertEquals("^\\/?[^/]*\\.java$", "*.java".globToRegex().pattern)
        assertEquals("^(?:.*\\/)?\\/?[^/]*\\.java$", "**/*.java".globToRegex().pattern)
        assertEquals("^\\/?[^/]*\\.js.$", "*.js?".globToRegex().pattern)
        assertEquals("^(?:.*\\/)?\\.gitignore$", "**/.gitignore".globToRegex().pattern)
        assertEquals("^\\/?[^/]*\\.js\\{on,\\}$", "*.js{on,}".globToRegex().pattern)
        assertEquals("^\\/?[^/]*\\.js\\*$", "*.js\\*".globToRegex().pattern)
        assertEquals("^/\\/?[^/]*$", "/*".globToRegex().pattern)
    }

}