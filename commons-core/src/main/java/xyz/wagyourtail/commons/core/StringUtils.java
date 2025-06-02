package xyz.wagyourtail.commons.core;

public class StringUtils {

    private StringUtils() {
    }

    public static int count(String str, char c) {
        int count = 0;
        for (char ch : str.toCharArray()) {
            if (ch == c) {
                count++;
            }
        }
        return count;
    }

    public static String translateEscapes(String str) {
        return translateEscapes(str, false);
    }

    public static String translateEscapes(String str, boolean lenient) {
        if (str.isEmpty() || !str.contains("\\")) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        int i = 0;
        while (i < str.length()) {
            char c = str.charAt(i++);
            if (c == '\\') {
                if (i >= str.length()) throw new IllegalArgumentException("Invalid escape, hit end of string");
                char n = str.charAt(i++);
                switch (n) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\'':
                        sb.append('\'');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                        int max;
                        if (n < '4') {
                            max = 2;
                        } else {
                            max = 1;
                        }
                        StringBuilder octal = new StringBuilder();
                        octal.append(n);
                        for (int j = 0; j < max; j++) {
                            if (i >= str.length()) break;
                            char next = str.charAt(i);
                            if (next < '0' || next > '7') break;
                            octal.append(next);
                            i++;
                        }
                        sb.append(Character.toChars(Integer.parseInt(octal.toString(), 8)));
                        break;
                    case 'u':
                        if (i + 4 > str.length())
                            throw new IllegalArgumentException("Invalid unicode escape, hit end of string");
                        String hex = str.substring(i, i + 4);
                        sb.append(Character.toChars(Integer.parseInt(hex, 16)));
                        i += 4;
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 's':
                        sb.append(' ');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    default:
                        if (lenient) {
                            sb.append('\\');
                            sb.append(n);
                        } else {
                            throw new IllegalArgumentException("Invalid escape: " + n + " in \"" + str + "\" at " + i);
                        }
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String escape(String str) {
        return escape(str, false, false, false);
    }

    public static String escape(String str, boolean unicode) {
        return escape(str, unicode, false, false);
    }

    public static String escape(String str, boolean unicode, boolean spaces, boolean doubleQuote) {
        if (str.isEmpty()) return str;
        StringBuilder sb = new StringBuilder(str.length());
        int i = 0;
        while (i < str.length()) {
            char c = str.charAt(i++);
            switch (c) {
                case '"':
                    if (doubleQuote) {
                        sb.append("\"\"");
                    } else {
                        sb.append("\\\"");
                    }
                    break;
                case '\'':
                    sb.append("\\'");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case ' ':
                    if (spaces) {
                        sb.append("\\s");
                    } else {
                        sb.append(c);
                    }
                    break;
                default:
                    if (unicode && (c < 0x20 || c > 0x7f)) {
                        sb.append("\\u").append(Integer.toHexString(c).toUpperCase());
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static String toHex(byte[] hexBytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : hexBytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    public static String capitalize(String str) {
        if (str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String uncapitalize(String str) {
        if (str.isEmpty()) return str;
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public static int indexOf(String str, char c, int startIndex, int endIndex) {
        int index = str.indexOf(c, startIndex);
        if (index >= endIndex) return -1;
        return index;
    }

}
