package xyz.wagyourtail.commons.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionUtils {

    private CollectionUtils() {
    }

    public static <T> int count(Iterable<T> iterable, T value) {
        int count = 0;
        for (T t : iterable) {
            if (t.equals(value)) {
                count++;
            }
        }
        return count;
    }

    public static List<String> resolveArgs(Map<String, String> properties, List<String> args, boolean hasDollar) {
        List<String> newArgs = new ArrayList<>();
        Pattern pattern = Pattern.compile((hasDollar ? "\\$" : "") + "\\{([^}]+)}");

        for (String arg : args) {
            Matcher m = pattern.matcher(arg);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String key = m.group(1);
                if (!properties.containsKey(key)) throw new IllegalArgumentException("Property " + key + " not found");
                String value = properties.get(key);
                m.appendReplacement(sb, value);
            }
            m.appendTail(sb);

            newArgs.add(sb.toString());
        }

        return newArgs;
    }

}
