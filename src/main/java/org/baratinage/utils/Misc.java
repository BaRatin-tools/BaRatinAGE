package org.baratinage.utils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.baratinage.ui.lg.Lg;

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

    public static String getTimeStamp() {
        Locale l = Lg.getLocale();
        LocalDateTime date = LocalDateTime.now();
        // SHORT format style omit seconds... which I need here.
        String text = date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(l));
        return text;
    }
}
