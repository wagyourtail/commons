package xyz.wagyourtail.commons.core;

import lombok.Getter;

import java.util.Arrays;
import java.util.Iterator;

@Getter
public enum AnsiColor {
    RESET("\u001B[0m"),
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    LIGHT_GRAY("\u001B[37m"),

    DARK_GRAY("\u001B[90m"),
    LIGHT_RED("\u001B[91m"),
    LIGHT_GREEN("\u001B[92m"),
    LIGHT_YELLOW("\u001B[93m"),
    LIGHT_BLUE("\u001B[94m"),
    LIGHT_PURPLE("\u001B[95m"),
    LIGHT_CYAN("\u001B[96m"),
    WHITE("\u001B[97m");

    private final String ansiColor;

    AnsiColor(String ansiColor) {
        this.ansiColor = ansiColor;
    }

    public String wrap(String message) {
        String[] parts = message.split("\n");
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = Arrays.asList(parts).iterator();
        while (it.hasNext()) {
            sb.append(ansiColor).append(it.next()).append(RESET.ansiColor);
            if (it.hasNext()) sb.append("\n");
        }
        return sb.toString();
    }

}
