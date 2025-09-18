package xyz.wagyourtail.commons.parsers.impl.semver;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.wagyourtail.commons.core.reader.ParseException;
import xyz.wagyourtail.commons.parsers.impl.SemVer;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// https://github.com/Masterminds/semver/blob/master/version_test.go
public class SemVerTest {

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void testSemVerParsing(String input, boolean expectedFail) {
        if (expectedFail) {
            assertThrows(ParseException.class, () -> SemVer.parse(input));
        } else {
            SemVer.parse(input);
        }
    }

    static Stream<Arguments> provideTestData() {
        Object[][] test = {
                {"1.2.3", false},
                {"1.2.3-alpha.01", true},
                {"1.2.3+test.01", false},
                {"1.2.3-alpha.-1", false},
                {"v1.2.3", true},
                {"1.0", true},
                {"v1.0", true},
                {"1", true},
                {"v1", true},
                {"1.2", true},
                {"1.2.beta", true},
                {"v1.2.beta", true},
                {"foo", true},
                {"1.2-5", true},
                {"v1.2-5", true},
                {"1.2-beta.5", true},
                {"v1.2-beta.5", true},
                {"\n1.2", true},
                {"\nv1.2", true},
                {"1.2.0-x.Y.0+metadata", false},
                {"v1.2.0-x.Y.0+metadata", true},
                {"1.2.0-x.Y.0+metadata-width-hypen", false},
                {"v1.2.0-x.Y.0+metadata-width-hypen", true},
                {"1.2.3-rc1-with-hypen", false},
                {"1.2.3-0abc123", false}, // string pre-releases can start with 0
                {"1.2.3-beta.01", true},  // number segment cannot start with 0
                {"v1.2.3-rc1-with-hypen", true},
                {"1.2.3.4", true},
                {"v1.2.3.4", true},
                {"1.2.2147483648", false},
                {"1.2147483648.3", false},
                {"2147483648.3.0", false},

                // The SemVer spec in a pre-release expects to allow [0-9A-Za-z-]. But,
                // the lack of all 3 parts in this version should produce an error.
                {"20221209-update-renovatejson-v4", true},

                // Various cases that are invalid semver
                {"1.1.2+.123", true},                             // A leading . in build metadata. This would signify that the first segment is empty
                {"1.0.0-alpha_beta", true},                       // An underscore in the pre-release is an invalid character
                {"1.0.0-alpha..", true},                          // Multiple empty segments
                {"1.0.0-alpha..1", true},                         // Multiple empty segments but one with a value
                {"01.1.1", true},                                 // A leading 0 on a number segment
                {"1.01.1", true},                                 // A leading 0 on a number segment
                {"1.1.01", true},                                 // A leading 0 on a number segment
                {"9.8.7+meta+meta", true},                        // Multiple metadata parts
                {"1.2.31----RC-SNAPSHOT.12.09.1--.12+788", true}, // Leading 0 in a number part of a pre-release segment
                {"1.2.3-0123", true},
                {"1.2.3-0123.0123", true},
                {"+invalid", true},
                {"-invalid", true},
                {"-invalid.01", true},
                {"alpha+beta", true},
                {"1.2.3-alpha_beta+foo", true},
                {"1.0.0-alpha..1", true},
        };

        return Arrays.stream(test).map(data -> Arguments.of(data[0], data[1]));
    }

}
