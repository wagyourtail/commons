package xyz.wagyourtail.commons.core.string;

import lombok.AllArgsConstructor;
import lombok.val;
import xyz.wagyourtail.commons.core.StringUtils;
import xyz.wagyourtail.commons.core.function.Function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@AllArgsConstructor
public class NameType {
    public static NameType CAMEL_CASE = new NameType(
            new Function<String, Iterable<String>>() {
                @Override
                public Iterable<String> apply(final String it) {
                    List<String> list = new ArrayList<>();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < it.length(); ++i) {
                        if (Character.isUpperCase(it.charAt(i))) {
                            if (sb.length() != 0 && Character.isLowerCase(sb.charAt(sb.length() - 1))) {
                                list.add(sb.toString().toLowerCase(Locale.ROOT));
                                sb.setLength(0);
                            }
                        } else if (sb.length() != 0) {
                            val lastChar = sb.charAt(sb.length() - 1);
                            if (sb.length() > 1 && Character.isUpperCase(lastChar)) {
                                // append multiple capitals and remove them from the sb
                                val s = sb.substring(0, sb.length() - 1);
                                list.add(!s.isEmpty() ? s : s.toLowerCase(Locale.ROOT));
                                sb.delete(0, sb.length() - 1);
                            }
                        }
                        sb.append(it.charAt(i));
                    }
                    if (sb.length() != 0) {
                        if (sb.length() > 1 && Character.isUpperCase(sb.charAt(0)) && Character.isUpperCase(sb.charAt(1))) {
                            list.add(sb.toString());
                        } else {
                            list.add(sb.toString().toLowerCase(Locale.ROOT));
                        }
                    }
                    return list;
                }
            },
            new Function<Iterable<String>, String>() {
                @Override
                public String apply(Iterable<String> strings) {
                    val iter = strings.iterator();
                    StringBuilder sb = new StringBuilder(iter.next());
                    while (iter.hasNext()) {
                        sb.append(StringUtils.capitalize(iter.next()));
                    }
                    return sb.toString();
                }
            }
    );

    public static NameType PASCAL_CASE = new NameType(
            CAMEL_CASE.split,
            new Function<Iterable<String>, String>() {
                @Override
                public String apply(Iterable<String> strings) {
                    val iter = strings.iterator();
                    StringBuilder sb = new StringBuilder();
                    while (iter.hasNext()) {
                        sb.append(StringUtils.capitalize(iter.next()));
                    }
                    return sb.toString();
                }
            }
    );

    public static NameType SNAKE_CASE = new NameType(
            new Function<String, Iterable<String>>() {
                @Override
                public Iterable<String> apply(String s) {
                    return Arrays.asList(s.split("_"));
                }
            },
            new Function<Iterable<String>, String>() {
                @Override
                public String apply(Iterable<String> strings) {
                    return StringUtils.joinToString("_", strings);
                }
            }
    );

    public static NameType KEBAB_CASE = new NameType(
            new Function<String, Iterable<String>>() {
                @Override
                public Iterable<String> apply(String s) {
                    return Arrays.asList(s.split("-"));
                }
            },
            new Function<Iterable<String>, String>() {
                @Override
                public String apply(Iterable<String> strings) {
                    return StringUtils.joinToString("-", strings);
                }
            }
    );

    private final Function<String, Iterable<String>> split;
    private final Function<Iterable<String>, String> join;


    public static String convert(String value, NameType from, NameType to) {
        return from.convert(to, value);
    }

    public String convert(NameType to, String value) {
        if (to == this) {
            return value;
        }

        if (value.isEmpty()) {
            return value;
        }

        // shortcuts
        // - pascal/camel
        if (this == PASCAL_CASE && to == CAMEL_CASE) {
            if (
                    (value.length() >= 3 && Character.isUpperCase(value.charAt(0)) && Character.isUpperCase(value.charAt(1)) && Character.isUpperCase(value.charAt(2))) ||
                            (value.length() == 2 && Character.isUpperCase(value.charAt(0)) && Character.isUpperCase(value.charAt(1)))
            ) {
                return value;
            } else {
                return StringUtils.uncapitalize(value);
            }
        }
        if (this == CAMEL_CASE && to == PASCAL_CASE) {
            return StringUtils.capitalize(value);
        }

        // - kebab/snake
        if (this == KEBAB_CASE && to == SNAKE_CASE) {
            return value.replace("-", "_");
        }
        if (this == SNAKE_CASE && to == KEBAB_CASE) {
            return value.replace("_", "-");
        }

        // fallback
        Iterable<String> split = this.split.apply(value);
        return to.join.apply(split);
    }

}
