package xyz.wagyourtail.commonskt.parsers.impl.semver.test

import xyz.wagyourtail.commonskt.parsers.impl.SemVer
import xyz.wagyourtail.commonskt.parsers.impl.SemVerRange
import kotlin.test.Test
import kotlin.test.assertEquals

// https://github.com/Masterminds/semver/blob/master/constraints_test.go
class SemVerRangeTest {

    private val testData = arrayOf(
        Triple("*", "1.2.3", true),
//        Triple("~0.0.0", "1.2.3", true),
        Triple("= 2.0", "1.2.3", false),
        Triple("= 2.0", "2.0.0", true),
        Triple("4.1", "4.1.0", true),
        Triple("4.1.x", "4.1.3", true),
        Triple("1.x", "1.4.0", true),
        Triple("!=4.1", "4.1.0", false),
        Triple("!=4.1", "5.1.0", true),
        Triple("!=4.x", "5.1.0", true),
        Triple("!=4.x", "4.1.0", false),
        Triple("!=4.1.x", "4.2.0", true),
        Triple("!=4.2.x", "4.2.3", false),
        Triple(">1.1", "4.1.0", true),
        Triple(">1.1", "1.1.0", false),
        Triple("<1.1", "0.1.0", true),
        Triple("<1.1", "1.1.0", false),
        Triple("<1.1", "1.1.1", false),
        Triple("<1.x", "1.1.1", false),
        Triple("<2.x", "1.1.1", true),
        Triple("<1.x", "2.1.1", false),
        Triple("<1.1.x", "1.2.1", false),
        Triple("<1.1.x", "1.1.500", false),
        Triple("<1.2.x", "1.1.1", true),
        Triple(">=1.1", "4.1.0", true),
        Triple(">=1.1", "1.1.0", true),
        Triple(">=1.1", "0.0.9", false),
        Triple("<=1.1", "0.1.0", true),
        Triple("<=1.1", "1.1.0", true),
        Triple("<=1.x", "1.1.0", true),
        Triple("<=2.x", "3.1.0", false),
        Triple("<=1.1", "1.1.1", true),
        Triple("<=1.1.x", "1.2.500", false),
        Triple(">1.1 <2", "1.1.1", false),
        Triple(">1.1 <2", "1.2.1", true),
        Triple(">1.1 <3", "4.3.2", false),
        Triple(">=1.1 <2 !=1.2.3", "1.2.3", false),
        Triple(">=1.1 <2 !=1.2.3 || > 3", "3.1.2", false),
        Triple(">=1.1 <2 !=1.2.3 || > 3", "4.1.2", true),
        Triple(">=1.1 <2 !=1.2.3 || >= 3", "3.0.0", true),
        Triple(">=1.1 <2 !=1.2.3 || > 3", "3.0.0", false),
        Triple(">=1.1 <2 !=1.2.3 || > 3", "1.2.3", false),
        Triple("1.1 - 2", "1.1.1", true),
        Triple("1.1-3", "4.3.2", false),
        Triple("[1.1,2)", "1.1.1", true),
        Triple("[1.1,3]", "4.3.2", false),
        Triple("^1.1", "1.1.1", true),
//        Triple("^1.1", "1.1.1-alpha", false),
        Triple("^1.1", "4.3.2", false),
        Triple("^1.x", "1.1.1", true),
        Triple("^2.x", "1.1.1", false),
        Triple("^1.x", "2.1.1", false),
        Triple("^0.0.1", "0.1.3", false),
        Triple("^0.0.1", "0.0.1", true),
        Triple("~*", "2.1.1", true),
        Triple("~1", "2.1.1", false),
        Triple("~1", "1.3.5", true),
//        Triple("~1", "1.3.5-beta", false),
        Triple("~1.x", "2.1.1", false),
        Triple("~1.x", "1.3.5", true),
//        Triple("~1.x", "1.3.5-beta", false),
        Triple("~1.x", "1.4.0", true),
        Triple("~1.1", "1.1.1", true),
        Triple("~1.2.3", "1.2.5", true),
        Triple("~1.2.3", "1.2.2", false),
        Triple("~1.2.3", "1.3.2", false),
        Triple("~1.1", "1.2.3", false),
        Triple("~1.3", "2.4.5", false),
    )

    @Test
    fun testSemVerComparing() {
        for ((input, version, expectedValue) in testData) {
            assertEquals(
                expectedValue,
                SemVerRange.checked(input).contains(SemVer.checked(version)),
                "expected $input ${if (expectedValue) "to contain" else "to not contain"} $version"
            )
        }
    }

}
