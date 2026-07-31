package xyz.wagyourtail.commons.core.string;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NameTypeTest {

    @Test
    public void testConvertCamelCaseToKebabCase() {
        assertEquals("test-test", NameType.CAMEL_CASE.convert(NameType.KEBAB_CASE, "testTest"));
        assertEquals("test-test-test", NameType.CAMEL_CASE.convert(NameType.KEBAB_CASE, "testTestTest"));
        // check with initialism
        assertEquals("THE-test", NameType.CAMEL_CASE.convert(NameType.KEBAB_CASE, "THETest"));
        assertEquals("test-THE", NameType.CAMEL_CASE.convert(NameType.KEBAB_CASE, "testTHE"));
    }

    @Test
    public void testConvertPascalCaseToKebabCase() {
        assertEquals("test-test", NameType.PASCAL_CASE.convert(NameType.KEBAB_CASE, "TestTest"));
        assertEquals("test-test-test", NameType.PASCAL_CASE.convert(NameType.KEBAB_CASE, "TestTestTest"));
        // check with initialism
        assertEquals("THE-test", NameType.PASCAL_CASE.convert(NameType.KEBAB_CASE, "THETest"));
        assertEquals("test-THE", NameType.PASCAL_CASE.convert(NameType.KEBAB_CASE, "TestTHE"));
    }

    @Test
    public void testConvertKebabCaseToCamelCase() {
        assertEquals("testTest", NameType.KEBAB_CASE.convert(NameType.CAMEL_CASE, "test-test"));
        assertEquals("testTestTest", NameType.KEBAB_CASE.convert(NameType.CAMEL_CASE, "test-test-test"));
        // check with initialism
        assertEquals("THETest", NameType.KEBAB_CASE.convert(NameType.CAMEL_CASE, "THE-test"));
        assertEquals("testTHE", NameType.KEBAB_CASE.convert(NameType.CAMEL_CASE, "test-THE"));
    }

    @Test
    public void testConvertKebabCaseToPascalCase() {
        assertEquals("TestTest", NameType.KEBAB_CASE.convert(NameType.PASCAL_CASE, "test-test"));
        assertEquals("TestTestTest", NameType.KEBAB_CASE.convert(NameType.PASCAL_CASE, "test-test-test"));
        // check with initialism
        assertEquals("THETest", NameType.KEBAB_CASE.convert(NameType.PASCAL_CASE, "THE-test"));
        assertEquals("TestTHE", NameType.KEBAB_CASE.convert(NameType.PASCAL_CASE, "test-THE"));
    }

    @Test
    public void testConvertCamelCaseToSnakeCase() {
        assertEquals("test_test", NameType.CAMEL_CASE.convert(NameType.SNAKE_CASE, "testTest"));
        assertEquals("test_test_test", NameType.CAMEL_CASE.convert(NameType.SNAKE_CASE, "testTestTest"));
        // check with initialism
        assertEquals("THE_test", NameType.CAMEL_CASE.convert(NameType.SNAKE_CASE, "THETest"));
        assertEquals("test_THE", NameType.CAMEL_CASE.convert(NameType.SNAKE_CASE, "testTHE"));
    }

    @Test
    public void testConvertPascalCaseToSnakeCase() {
        assertEquals("test_test", NameType.PASCAL_CASE.convert(NameType.SNAKE_CASE, "TestTest"));
        assertEquals("test_test_test", NameType.PASCAL_CASE.convert(NameType.SNAKE_CASE, "TestTestTest"));
        // check with initialism
        assertEquals("THE_test", NameType.PASCAL_CASE.convert(NameType.SNAKE_CASE, "THETest"));
        assertEquals("test_THE", NameType.PASCAL_CASE.convert(NameType.SNAKE_CASE, "TestTHE"));
    }

    @Test
    public void testConvertSnakeCaseToCamelCase() {
        assertEquals("testTest", NameType.SNAKE_CASE.convert(NameType.CAMEL_CASE, "test_test"));
        assertEquals("testTestTest", NameType.SNAKE_CASE.convert(NameType.CAMEL_CASE, "test_test_test"));
        // check with initialism
        assertEquals("THETest", NameType.SNAKE_CASE.convert(NameType.CAMEL_CASE, "THE_test"));
        assertEquals("testTHE", NameType.SNAKE_CASE.convert(NameType.CAMEL_CASE, "test_THE"));
    }

    @Test
    public void testConvertSnakeCaseToPascalCase() {
        assertEquals("TestTest", NameType.SNAKE_CASE.convert(NameType.PASCAL_CASE, "test_test"));
        assertEquals("TestTestTest", NameType.SNAKE_CASE.convert(NameType.PASCAL_CASE, "test_test_test"));
        // check with initialism
        assertEquals("THETest", NameType.SNAKE_CASE.convert(NameType.PASCAL_CASE, "THE_test"));
        assertEquals("TestTHE", NameType.SNAKE_CASE.convert(NameType.PASCAL_CASE, "test_THE"));
    }

    @Test
    public void testConvertCamelCaseToPascalCase() {
        assertEquals("TestTest", NameType.CAMEL_CASE.convert(NameType.PASCAL_CASE, "testTest"));
        assertEquals("TestTestTest", NameType.CAMEL_CASE.convert(NameType.PASCAL_CASE, "testTestTest"));
        // check with initialism
        assertEquals("THETest", NameType.CAMEL_CASE.convert(NameType.PASCAL_CASE, "THETest"));
        assertEquals("TestTHE", NameType.CAMEL_CASE.convert(NameType.PASCAL_CASE, "testTHE"));
    }

    @Test
    public void testConvertPascalCaseToCamelCase() {
        assertEquals("testTest", NameType.PASCAL_CASE.convert(NameType.CAMEL_CASE, "TestTest"));
        assertEquals("testTestTest", NameType.PASCAL_CASE.convert(NameType.CAMEL_CASE, "TestTestTest"));
        // check with initialism
        assertEquals("THETest", NameType.PASCAL_CASE.convert(NameType.CAMEL_CASE, "THETest"));
        assertEquals("testTHE", NameType.PASCAL_CASE.convert(NameType.CAMEL_CASE, "TestTHE"));
    }

}
