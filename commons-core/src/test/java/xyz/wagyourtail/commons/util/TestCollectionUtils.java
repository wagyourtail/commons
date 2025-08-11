package xyz.wagyourtail.commons.util;

import org.junit.jupiter.api.Test;
import xyz.wagyourtail.commons.core.CollectionUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestCollectionUtils {

    public void testResolveArgs() {
        var map = Map.of(
                "input", "example.jar",
                "output", "output.jar",
                "wasd", "qwer"
        );
        var args = List.of(
                "{input}",
                "--output={output}",
                "{wasd}",
                "nothing"
        );

        var result = CollectionUtils.resolveArgs(map, args, false);

        assertEquals(
                List.of(
                        "example.jar",
                        "--output=output.jar",
                        "qwer",
                        "nothing"
                ),
                result
        );
    }

    public void testResolveArgs2() {
        var map = Map.of(
                "input", "example.jar",
                "output", "output.jar",
                "wasd", "qwer"
        );
        var args = List.of(
                "${input}",
                "--output=${output}",
                "${wasd}",
                "nothing"
        );

        var result = CollectionUtils.resolveArgs(map, args, true);

        assertEquals(
                List.of(
                        "example.jar",
                        "--output=output.jar",
                        "qwer",
                        "nothing"
                ),
                result
        );
    }

    @Test
    public void testMissingArg() {
        assertThrows(IllegalArgumentException.class, () -> {
            var map = Map.of(
                    "input", "example.jar",
                    "output", "output.jar"
            );
            var args = List.of(
                    "${input}",
                    "--output=${output}",
                    "${wasd}",
                    "nothing"
            );

            CollectionUtils.resolveArgs(map, args, true);
        }, "Property wasd not found");
    }

}
