package xyz.wagyourtail.commons.logger;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.wagyourtail.commons.core.AnsiColor;
import xyz.wagyourtail.commons.core.logger.Logger;
import xyz.wagyourtail.commons.core.logger.SimpleLogger;
import xyz.wagyourtail.commons.core.logger.prefix.DefaultLoggingPrefix;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SimpleLoggerTest {

    @Test
    public void test() {
        TestPrintStream printStream = new TestPrintStream();
        val builder = DefaultLoggingPrefix.builder()
                .includeTime(false)
                .includeThreadName(false);

        Logger logger = SimpleLogger.builder()
                .prefix(builder.build())
                .level(Logger.Level.DEBUG)
                .useAnsiColors(false)
                .out(printStream)
                .build();

        logger.log(Logger.Level.DEBUG, "test");
        assertEquals("[SimpleLoggerTest] [DEBUG] test", printStream.getLine());

        logger.log(Logger.Level.INFO, "test2");
        assertEquals("[SimpleLoggerTest] [INFO] test2", printStream.getLine());

        logger.log(Logger.Level.TRACE, "test3");
        assertNull(printStream.getLine());


        Logger logger2 = logger.subLogger("sublogger");
        logger2.log(Logger.Level.INFO, "test4");
        assertEquals("[SimpleLoggerTest/sublogger] [INFO] test4", printStream.getLine());

        Logger logger3 = SimpleLogger.builder()
                .prefix(builder.build())
                .level(Logger.Level.DEBUG)
                .out(printStream)
                .build();

        logger3.log(Logger.Level.DEBUG, "test5");
        assertEquals(AnsiColor.LIGHT_GRAY.wrap("[SimpleLoggerTest] [DEBUG] test5"), printStream.getLine());

        logger3.log(Logger.Level.INFO, "test6");
        assertEquals(AnsiColor.WHITE.wrap("[SimpleLoggerTest] [INFO] test6"), printStream.getLine());

        assertNull(printStream.getLine());

        logger2.wrapPrintStream(Logger.Level.DEBUG, ps -> {
            ps.println("test7");
            assertEquals("[SimpleLoggerTest/sublogger] [DEBUG] test7", printStream.getLine());
            ps.println("test8");
            assertEquals("[SimpleLoggerTest/sublogger] [DEBUG] test8", printStream.getLine());
        });

        assertNull(printStream.getLine());
    }

    public static class TestPrintStream extends PrintStream {
        String line = null;

        public TestPrintStream() {
            super((OutputStream) null, true, Charset.defaultCharset());
        }

        @Override
        public void println(String line) {
            if (this.line != null) {
                throw new IllegalStateException();
            }
            this.line = line;
        }

        public String getLine() {
            String line = this.line;
            this.line = null;
            return line;
        }

    }

}
