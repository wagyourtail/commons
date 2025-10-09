package xyz.wagyourtail.commons.parsers.impl.semver;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.wagyourtail.commons.parsers.impl.SemVer;
import xyz.wagyourtail.commons.parsers.impl.SemVerRange;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// https://github.com/Masterminds/semver/blob/master/constraints_test.go
public class SemVerRangeTest {

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void testSemVerComparing(String input, String version, boolean expectedValue) {
        assertEquals(expectedValue, SemVerRange.parse(input).contains(SemVer.parse(version)));
    }

    static Stream<Arguments> provideTestData() {
        Object[][] test = {
            {"*", "1.2.3", true},
//            {"~0.0.0", "1.2.3", true},
            {"= 2.0", "1.2.3", false},
            {"= 2.0", "2.0.0", true},
            {"4.1", "4.1.0", true},
            {"4.1.x", "4.1.3", true},
            {"1.x", "1.4.0", true},
            {"!=4.1", "4.1.0", false},
            {"!=4.1", "5.1.0", true},
            {"!=4.x", "5.1.0", true},
            {"!=4.x", "4.1.0", false},
            {"!=4.1.x", "4.2.0", true},
            {"!=4.2.x", "4.2.3", false},
            {">1.1", "4.1.0", true},
            {">1.1", "1.1.0", false},
            {"<1.1", "0.1.0", true},
            {"<1.1", "1.1.0", false},
            {"<1.1", "1.1.1", false},
            {"<1.x", "1.1.1", false},
            {"<2.x", "1.1.1", true},
            {"<1.x", "2.1.1", false},
            {"<1.1.x", "1.2.1", false},
            {"<1.1.x", "1.1.500", false},
            {"<1.2.x", "1.1.1", true},
            {">=1.1", "4.1.0", true},
            {">=1.1", "1.1.0", true},
            {">=1.1", "0.0.9", false},
            {"<=1.1", "0.1.0", true},
            {"<=1.1", "1.1.0", true},
            {"<=1.x", "1.1.0", true},
            {"<=2.x", "3.1.0", false},
            {"<=1.1", "1.1.1", true},
            {"<=1.1.x", "1.2.500", false},
            {">1.1 <2", "1.1.1", false},
            {">1.1 <2", "1.2.1", true},
            {">1.1 <3", "4.3.2", false},
            {">=1.1 <2 !=1.2.3", "1.2.3", false},
            {">=1.1 <2 !=1.2.3 || > 3", "3.1.2", false},
            {">=1.1 <2 !=1.2.3 || > 3", "4.1.2", true},
            {">=1.1 <2 !=1.2.3 || >= 3", "3.0.0", true},
            {">=1.1 <2 !=1.2.3 || > 3", "3.0.0", false},
            {">=1.1 <2 !=1.2.3 || > 3", "1.2.3", false},
            {"1.1 - 2", "1.1.1", true},
            {"1.1-3", "4.3.2", false},
            {"[1.1,2)", "1.1.1", true},
            {"[1.1,3]", "4.3.2", false},
            {"^1.1", "1.1.1", true},
//            {"^1.1", "1.1.1-alpha", false},
            {"^1.1", "4.3.2", false},
            {"^1.x", "1.1.1", true},
            {"^2.x", "1.1.1", false},
            {"^1.x", "2.1.1", false},
            {"^0.0.1", "0.1.3", false},
            {"^0.0.1", "0.0.1", true},
            {"~*", "2.1.1", true},
            {"~1", "2.1.1", false},
            {"~1", "1.3.5", true},
//            {"~1", "1.3.5-beta", false},
            {"~1.x", "2.1.1", false},
            {"~1.x", "1.3.5", true},
//            {"~1.x", "1.3.5-beta", false},
            {"~1.x", "1.4.0", true},
            {"~1.1", "1.1.1", true},
            {"~1.2.3", "1.2.5", true},
            {"~1.2.3", "1.2.2", false},
            {"~1.2.3", "1.3.2", false},
            {"~1.1", "1.2.3", false},
            {"~1.3", "2.4.5", false},
        };

        return Arrays.stream(test).map(data -> Arguments.of(data[0], data[1], data[2]));
    }

}
