package xyz.wagyourtail.commons.core.console;

import xyz.wagyourtail.commons.core.AnsiColor;

import java.util.Arrays;
import java.util.Iterator;

public class ConsoleFormatting {
    public static final ConsoleFormatting RESET = new ConsoleFormatting(0);

    public static final ConsoleFormatting BOLD = new ConsoleFormatting(1);
    public static final ConsoleFormatting DIM = new ConsoleFormatting(2);
    public static final ConsoleFormatting ITALIC = new ConsoleFormatting(3);
    public static final ConsoleFormatting UNDERLINE = new ConsoleFormatting(4);
    public static final ConsoleFormatting SLOW_BLINK = new ConsoleFormatting(5);

    /**
     * not widely supported
     */
    public static final ConsoleFormatting FAST_BLINK = new ConsoleFormatting(6);
    public static final ConsoleFormatting REVERSE = new ConsoleFormatting(7);

    /**
     * not widely supported
     */
    public static final ConsoleFormatting HIDDEN = new ConsoleFormatting(8);
    public static final ConsoleFormatting STRIKETHROUGH = new ConsoleFormatting(9);

    /**
     * Double-underline per ECMA-48, but undoes bold on several terminals
     */
    public static final ConsoleFormatting DOUBLE_UNDERLINE = new ConsoleFormatting(21);

    public static final ConsoleFormatting RESET_INTENSITY = new ConsoleFormatting(22);
    public static final ConsoleFormatting RESET_ITALIC = new ConsoleFormatting(23);
    public static final ConsoleFormatting RESET_UNDERLINE = new ConsoleFormatting(24);
    public static final ConsoleFormatting RESET_SLOW_BLINK = new ConsoleFormatting(25);

    /**
     * not widely supported
     */
    public static final ConsoleFormatting PROPORTIONAL_SPACING = new ConsoleFormatting(26);

    public static final ConsoleFormatting RESET_HIDDEN = new ConsoleFormatting(28);
    public static final ConsoleFormatting RESET_STRIKETHROUGH = new ConsoleFormatting(29);

    public static final ConsoleFormatting RESET_FOREGROUND_COLOR = new ConsoleFormatting(39);
    public static final ConsoleFormatting RESET_BACKGROUND_COLOR = new ConsoleFormatting(49);

    public static final ConsoleFormatting RESET_PROPORTIONAL_SPACING = new ConsoleFormatting(50);

    public static final ConsoleFormatting FRAMED = new ConsoleFormatting(51);
    public static final ConsoleFormatting CIRCLED = new ConsoleFormatting(52);
    public static final ConsoleFormatting OVERLINED = new ConsoleFormatting(53);

    /**
     * resets both framed and circled
     */
    public static final ConsoleFormatting RESET_FRAMED = new ConsoleFormatting(54);
    public static final ConsoleFormatting RESET_OVERLINED = new ConsoleFormatting(55);

    public static final ConsoleFormatting RESET_UNDERLINE_COLOR = new ConsoleFormatting(59);

    /**
     * not widely supported
     */
    public static final ConsoleFormatting IDEOGRAM_UNDERLINE = new ConsoleFormatting(60);

    /**
     * not widely supported
     */
    public static final ConsoleFormatting IDEOGRAM_DOUBLE_UNDERLINE = new ConsoleFormatting(61);

    /**
     * not widely supported
     */
    public static final ConsoleFormatting IDEOGRAM_OVERLINE = new ConsoleFormatting(62);

    /**
     * not widely supported
     */
    public static final ConsoleFormatting IDEOGRAM_DOUBLE_OVERLINE = new ConsoleFormatting(63);

    /**
     * not widely supported
     */
    public static final ConsoleFormatting IDEOGRAM_STRESS_MARKING = new ConsoleFormatting(64);

    public static final ConsoleFormatting RESET_IDEOGRAM = new ConsoleFormatting(65);

    /**
     * not widely supported
     */
    public static final ConsoleFormatting SUPERSCRIPT = new ConsoleFormatting(73);
    /**
     * not widely supported
     */
    public static final ConsoleFormatting SUBSCRIPT = new ConsoleFormatting(74);

    /**
     * resets both superscript and subscript
     */
    public static final  ConsoleFormatting RESET_SUPERSCRIPT = new ConsoleFormatting(75);

    private final int[] value;

    public ConsoleFormatting(int... value) {
        this.value = value;
    }

    public static ConsoleFormatting concat(ConsoleFormatting... formattings) {
        int i = 0;
        for (ConsoleFormatting formatting : formattings) {
            i += formatting.value.length;
        }
        int[] values = new int[i];
        i = 0;
        for (ConsoleFormatting formatting : formattings) {
            System.arraycopy(formatting.value, 0, values, i, formatting.value.length);
            i += formatting.value.length;
        }
        return new ConsoleFormatting(values);
    }

    public ConsoleFormatting concat(ConsoleFormatting other) {
        int[] values = new int[value.length + other.value.length];
        System.arraycopy(value, 0, values, 0, value.length);
        System.arraycopy(other.value, 0, values, value.length, other.value.length);
        return new ConsoleFormatting(values);
    }

    public String getFormatCode() {
        StringBuilder sb = new StringBuilder("\033[");
        sb.append(value[0]);
        if (value.length > 1) {
            for (int i = 1; i < value.length; i++) {
                sb.append(';').append(value[i]);
            }
        }
        sb.append('m');
        return sb.toString();
    }

    public String wrap(String message) {
        String[] parts = message.split("\n");
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = Arrays.asList(parts).iterator();
        while (it.hasNext()) {
            sb.append(getFormatCode()).append(it.next()).append(RESET.getFormatCode());
            if (it.hasNext()) sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * font 10 is not widely supported
     * @param font index of 0-10
     */
    public static ConsoleFormatting font(byte font) {
        return new ConsoleFormatting(10 + font);
    }

    /**
     * @param color index of color 0-15
     * @return a forground color in the <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#3-bit_and_4-bit">3/4 bit table</a>
     */
    public static ConsoleFormatting foregroundANSI(byte color) {
        return AnsiColor.values()[color + 1].foreground;
    }

    public static ConsoleFormatting backgroundANSI(byte color) {
        return AnsiColor.values()[color + 1].background;
    }

    /**
     * @return a foreground color in the <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#8-bit">256 color palette</a>
     */
    public static ConsoleFormatting foreground8(byte color) {
        return new ConsoleFormatting(38, 5, color);
    }

    public static ConsoleFormatting background8(byte color) {
        return new ConsoleFormatting(48, 5, color);
    }

    public static ConsoleFormatting underline8(byte color) {
        return new ConsoleFormatting(58, 5, color);
    }

    /**
     * @return a foreground color in the <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#8-bit">256 color palette</a>
     */
    public static ConsoleFormatting foreground24(byte r, byte g, byte b) {
        return new ConsoleFormatting(38, 2, r, g, b);
    }

    public static ConsoleFormatting background24(byte r, byte g, byte b) {
        return new ConsoleFormatting(48, 2, r, g, b);
    }

    public static ConsoleFormatting underline24(byte r, byte g, byte b) {
        return new ConsoleFormatting(58, 2, r, g, b);
    }

}
