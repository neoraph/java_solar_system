package com.raph.solarsystem.i18n;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public final class I18n {
    public static final Locale EN = Locale.ENGLISH;
    public static final Locale FR = Locale.FRENCH;
    public static final Locale JA = Locale.JAPANESE;

    private I18n() {}

    public static List<Locale> supportedLocales() {
        return List.of(EN, FR, JA);
    }

    public static Locale normalize(Locale locale) {
        String language = locale == null ? "" : locale.getLanguage();
        if (FR.getLanguage().equals(language)) {
            return FR;
        }
        if (JA.getLanguage().equals(language)) {
            return JA;
        }
        return EN;
    }

    public static ResourceBundle bundle(Locale locale) {
        return ResourceBundle.getBundle("i18n.messages", normalize(locale));
    }

    public static String tr(Locale locale, String key, Object... args) {
        ResourceBundle bundle = bundle(locale);
        String pattern = bundle.getString(key);
        MessageFormat format = new MessageFormat(pattern, normalize(locale));
        return format.format(args);
    }

    public static String trOrDefault(Locale locale, String key, String fallback, Object... args) {
        ResourceBundle bundle = bundle(locale);
        String pattern = bundle.containsKey(key) ? bundle.getString(key) : fallback;
        MessageFormat format = new MessageFormat(pattern, normalize(locale));
        return format.format(args);
    }

    public static String planetName(Locale locale, String englishName) {
        return trOrDefault(locale, "planet." + englishName, englishName);
    }
}
