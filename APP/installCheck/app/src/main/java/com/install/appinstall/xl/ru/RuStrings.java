package com.install.appinstall.xl.ru;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Locale-gated translation for strings rendered by the module's dynamic UI. */
public final class RuStrings {
    private static final int MAX_CACHE_SIZE = 4096;
    private static final Map<String, String> TRANSLATIONS = RuCatalog.create();
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private RuStrings() {}

    public static String translateString(String source) {
        if (source == null || !isRussian()) return source;

        String exact = TRANSLATIONS.get(source);
        if (exact != null) return exact;
        if (!containsHan(source)) return source;

        String cached = CACHE.get(source);
        if (cached != null) return cached;

        String translated = source;
        for (Map.Entry<String, String> entry : TRANSLATIONS.entrySet()) {
            if (translated.contains(entry.getKey())) {
                translated = translated.replace(entry.getKey(), entry.getValue());
            }
        }
        if (CACHE.size() >= MAX_CACHE_SIZE) CACHE.clear();
        CACHE.put(source, translated);
        return translated;
    }

    public static CharSequence translate(CharSequence source) {
        return source instanceof String ? translateString((String) source) : source;
    }

    public static CharSequence[] translateArray(CharSequence[] source) {
        if (source == null || !isRussian()) return source;
        CharSequence[] translated = source.clone();
        for (int index = 0; index < translated.length; index++) {
            translated[index] = translate(translated[index]);
        }
        return translated;
    }

    public static String[] translateArray(String[] source) {
        if (source == null || !isRussian()) return source;
        String[] translated = source.clone();
        for (int index = 0; index < translated.length; index++) {
            translated[index] = translateString(translated[index]);
        }
        return translated;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        return Html.fromHtml(translateString(source));
    }

    public static Spanned fromHtml(String source, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(translateString(source), flags);
        }
        return fromHtml(source);
    }

    private static boolean isRussian() {
        return "ru".equals(Locale.getDefault().getLanguage());
    }

    private static boolean containsHan(String value) {
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character >= '\u3400' && character <= '\u9fff') return true;
        }
        return false;
    }
}
