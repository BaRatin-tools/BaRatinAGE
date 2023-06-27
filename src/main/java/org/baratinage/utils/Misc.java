package org.baratinage.utils;

import java.io.File;

public class Misc {

    // source: https://stackoverflow.com/a/24692712
    public static String sanitizeName(String name) {
        if (null == name) {
            return "";
        }

        if (File.separatorChar == '/') {
            return name.replaceAll("[\u0000/]+", "").trim();
        }

        return name.replaceAll("[\u0000-\u001f<>:\"/\\\\|?*\u007f]+", "").trim();
    }
}
