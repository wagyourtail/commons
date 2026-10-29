package xyz.wagyourtail.commonskt.parsers.impl.semver.test

import xyz.wagyourtail.commonskt.parsers.impl.SemVer
import xyz.wagyourtail.commonskt.reader.ParseException
import kotlin.test.Test
import kotlin.test.assertFailsWith

// https://github.com/Masterminds/semver/blob/master/version_test.go
class SemVerTest {

    private val testData = arrayOf(
        "1.2.3" to false,
        "1.2.3-alpha.01" to true,
        "1.2.3+test.01" to false,
        "1.2.3-alpha.-1" to false,
        "v1.2.3" to true,
        "1.0" to true,
        "v1.0" to true,
        "1" to true,
        "v1" to true,
        "1.2" to true,
        "1.2.beta" to true,
        "v1.2.beta" to true,
        "foo" to true,
        "1.2-5" to true,
        "v1.2-5" to true,
        "1.2-beta.5" to true,
        "v1.2-beta.5" to true,
        "\n1.2" to true,
        "\nv1.2" to true,
        "1.2.0-x.Y.0+metadata" to false,
        "v1.2.0-x.Y.0+metadata" to true,
        "1.2.0-x.Y.0+metadata-width-hypen" to false,
        "v1.2.0-x.Y.0+metadata-width-hypen" to true,
        "1.2.3-rc1-with-hypen" to false,
        "1.2.3-0abc123" to false, // string pre-releases can start with 0
        "1.2.3-beta.01" to true,  // number segment cannot start with 0
        "v1.2.3-rc1-with-hypen" to true,
        "1.2.3.4" to true,
        "v1.2.3.4" to true,
        "1.2.2147483648" to false,
        "1.2147483648.3" to false,
        "2147483648.3.0" to false,

        // The SemVer spec in a pre-release expects to allow [0-9A-Za-z-]. But,
        // the lack of all 3 parts in this version should produce an error.
        "20221209-update-renovatejson-v4" to true,

        // Various cases that are invalid semver
        "1.1.2+.123" to true,                             // A leading . in build metadata. This would signify that the first segment is empty
        "1.0.0-alpha_beta" to true,                       // An underscore in the pre-release is an invalid character
        "1.0.0-alpha.." to true,                          // Multiple empty segments
        "1.0.0-alpha..1" to true,                         // Multiple empty segments but one with a value
        "01.1.1" to true,                                 // A leading 0 on a number segment
        "1.01.1" to true,                                 // A leading 0 on a number segment
        "1.1.01" to true,                                 // A leading 0 on a number segment
        "9.8.7+meta+meta" to true,                        // Multiple metadata parts
        "1.2.31----RC-SNAPSHOT.12.09.1--.12+788" to true, // Leading 0 in a number part of a pre-release segment
        "1.2.3-0123" to true,
        "1.2.3-0123.0123" to true,
        "+invalid" to true,
        "-invalid" to true,
        "-invalid.01" to true,
        "alpha+beta" to true,
        "1.2.3-alpha_beta+foo" to true,
        "1.0.0-alpha..1" to true,
    )

    @Test
    fun testSemVerParsing() {
        for ((input, expectedFail) in testData) {
            if (expectedFail) {
                assertFailsWith(ParseException::class, "expected $input to fail") {
                    SemVer.checked(input)
                }
            } else {
                SemVer.checked(input)
            }
        }
    }

}
