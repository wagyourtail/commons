package xyz.wagyourtail.commonskt.test.string

import xyz.wagyourtail.commonskt.string.NameType
import xyz.wagyourtail.commonskt.string.convertNameType
import kotlin.test.Test
import kotlin.test.assertEquals

class NameTypeTest {

    @Test
    fun testConvertCamelCaseToKebabCase() {
        assertEquals("test-test", "testTest".convertNameType(NameType.CAMEL_CASE, NameType.KEBAB_CASE))
        assertEquals("test-test-test", "testTestTest".convertNameType(NameType.CAMEL_CASE, NameType.KEBAB_CASE))
        // check with initialism
        assertEquals("THE-test", "THETest".convertNameType(NameType.CAMEL_CASE, NameType.KEBAB_CASE))
        assertEquals("test-THE", "testTHE".convertNameType(NameType.CAMEL_CASE, NameType.KEBAB_CASE))
    }

    @Test
    fun testConvertPascalCaseToKebabCase() {
        assertEquals("test-test", "TestTest".convertNameType(NameType.PASCAL_CASE, NameType.KEBAB_CASE))
        assertEquals("test-test-test", "TestTestTest".convertNameType(NameType.PASCAL_CASE, NameType.KEBAB_CASE))
        // check with initialism
        assertEquals("THE-test", "THETest".convertNameType(NameType.PASCAL_CASE, NameType.KEBAB_CASE))
        assertEquals("test-THE", "TestTHE".convertNameType(NameType.PASCAL_CASE, NameType.KEBAB_CASE))
    }

    @Test
    fun testConvertKebabCaseToCamelCase() {
        assertEquals("testTest", "test-test".convertNameType(NameType.KEBAB_CASE, NameType.CAMEL_CASE))
        assertEquals("testTestTest", "test-test-test".convertNameType(NameType.KEBAB_CASE, NameType.CAMEL_CASE))
        // check with initialism
        assertEquals("THETest", "THE-test".convertNameType(NameType.KEBAB_CASE, NameType.CAMEL_CASE))
        assertEquals("testTHE", "test-THE".convertNameType(NameType.KEBAB_CASE, NameType.CAMEL_CASE))
    }

    @Test
    fun testConvertKebabCaseToPascalCase() {
        assertEquals("TestTest", "test-test".convertNameType(NameType.KEBAB_CASE, NameType.PASCAL_CASE))
        assertEquals("TestTestTest", "test-test-test".convertNameType(NameType.KEBAB_CASE, NameType.PASCAL_CASE))
        // check with initialism
        assertEquals("THETest", "THE-test".convertNameType(NameType.KEBAB_CASE, NameType.PASCAL_CASE))
        assertEquals("TestTHE", "test-THE".convertNameType(NameType.KEBAB_CASE, NameType.PASCAL_CASE))
    }

    @Test
    fun testConvertCamelCaseToSnakeCase() {
        assertEquals("test_test", "testTest".convertNameType(NameType.CAMEL_CASE, NameType.SNAKE_CASE))
        assertEquals("test_test_test", "testTestTest".convertNameType(NameType.CAMEL_CASE, NameType.SNAKE_CASE))
        // check with initialism
        assertEquals("THE_test", "THETest".convertNameType(NameType.CAMEL_CASE, NameType.SNAKE_CASE))
        assertEquals("test_THE", "testTHE".convertNameType(NameType.CAMEL_CASE, NameType.SNAKE_CASE))
    }

    @Test
    fun testConvertPascalCaseToSnakeCase() {
        assertEquals("test_test", "TestTest".convertNameType(NameType.PASCAL_CASE, NameType.SNAKE_CASE))
        assertEquals("test_test_test", "TestTestTest".convertNameType(NameType.PASCAL_CASE, NameType.SNAKE_CASE))
        // check with initialism
        assertEquals("THE_test", "THETest".convertNameType(NameType.PASCAL_CASE, NameType.SNAKE_CASE))
        assertEquals("test_THE", "TestTHE".convertNameType(NameType.PASCAL_CASE, NameType.SNAKE_CASE))
    }

    @Test
    fun testConvertSnakeCaseToCamelCase() {
        assertEquals("testTest", "test_test".convertNameType(NameType.SNAKE_CASE, NameType.CAMEL_CASE))
        assertEquals("testTestTest", "test_test_test".convertNameType(NameType.SNAKE_CASE, NameType.CAMEL_CASE))
        // check with initialism
        assertEquals("THETest", "THE_test".convertNameType(NameType.SNAKE_CASE, NameType.CAMEL_CASE))
        assertEquals("testTHE", "test_THE".convertNameType(NameType.SNAKE_CASE, NameType.CAMEL_CASE))
    }

    @Test
    fun testConvertSnakeCaseToPascalCase() {
        assertEquals("TestTest", "test_test".convertNameType(NameType.SNAKE_CASE, NameType.PASCAL_CASE))
        assertEquals("TestTestTest", "test_test_test".convertNameType(NameType.SNAKE_CASE, NameType.PASCAL_CASE))
        // check with initialism
        assertEquals("THETest", "THE_test".convertNameType(NameType.SNAKE_CASE, NameType.PASCAL_CASE))
        assertEquals("TestTHE", "test_THE".convertNameType(NameType.SNAKE_CASE, NameType.PASCAL_CASE))
    }

    @Test
    fun testConvertCamelCaseToPascalCase() {
        assertEquals("TestTest", "testTest".convertNameType(NameType.CAMEL_CASE, NameType.PASCAL_CASE))
        assertEquals("TestTestTest", "testTestTest".convertNameType(NameType.CAMEL_CASE, NameType.PASCAL_CASE))
        // check with initialism
        assertEquals("THETest", "THETest".convertNameType(NameType.CAMEL_CASE, NameType.PASCAL_CASE))
        assertEquals("TestTHE", "testTHE".convertNameType(NameType.CAMEL_CASE, NameType.PASCAL_CASE))
    }

    @Test
    fun testConvertPascalCaseToCamelCase() {
        assertEquals("testTest", "TestTest".convertNameType(NameType.PASCAL_CASE, NameType.CAMEL_CASE))
        assertEquals("testTestTest", "TestTestTest".convertNameType(NameType.PASCAL_CASE, NameType.CAMEL_CASE))
        // check with initialism
        assertEquals("THETest", "THETest".convertNameType(NameType.PASCAL_CASE, NameType.CAMEL_CASE))
        assertEquals("testTHE", "TestTHE".convertNameType(NameType.PASCAL_CASE, NameType.CAMEL_CASE))
    }

}