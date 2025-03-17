package xyz.wagyourtail.commons.core;

import java.util.Locale;

public class OSUtils {
    public static String WINDOWS = "windows";
    public static String LINUX = "linux";
    public static String OSX = "osx";
    public static String UNKNOWN = "unknown";

    private OSUtils() {}

    public static String getOsName() {
        return System.getProperty("os.name");
    }

    public static String getOsId() {
        String osName = getOsName().toLowerCase(Locale.getDefault());
        if (osName.contains("darwin") || osName.contains("mac")) {
            return OSX;
        }
        if (osName.contains("win")) {
            return WINDOWS;
        }
        if (osName.contains("nux")) {
           return  LINUX;
        }
        return UNKNOWN;
    }

    public static String getOsVersion() {
        return System.getProperty("os.version");
    }

    public static String getOsArch() {
        return System.getProperty("os.arch");
    }

    public static int getOsArchNumeric() {
        switch (getOsArch()) {
            case "x86":
            case "i386":
            case "i686":
                return 32;
            case "amd64":
            case "aarch64":
            case "x86_64":
                return 64;
            default:
                return -1;
        }
    }

}
