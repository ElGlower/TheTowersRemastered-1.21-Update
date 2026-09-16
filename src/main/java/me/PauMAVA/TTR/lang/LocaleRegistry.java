package me.PauMAVA.TTR.lang;

import java.util.ArrayList;
import java.util.List;

public class LocaleRegistry {

    private static final List<Locale> locales = new ArrayList<>();

    public static void registerLocale(Locale locale) {
        locales.add(locale);
    }

    public static List<Locale> getLocales() {
        return locales;
    }

    public static Locale getLocaleByShortName(String shortName) {
        for (Locale locale : locales) {
            if (locale.getShortName().equalsIgnoreCase(shortName)) {
                return locale;
            }
        }
        return null;
    }
}