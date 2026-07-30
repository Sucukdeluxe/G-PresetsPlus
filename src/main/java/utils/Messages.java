package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Messages {

    public enum Language {
        EN("English"),
        DE("Deutsch");

        private final String label;

        Language(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static Language fromName(String name, Language fallback) {
            if (name == null) return fallback;
            for (Language language : values()) {
                if (language.name().equalsIgnoreCase(name.trim())) {
                    return language;
                }
            }
            return fallback;
        }
    }

    private static final Map<String, String> EN = new HashMap<>();
    private static final Map<String, String> DE = new HashMap<>();
    private static final List<Runnable> listeners = new ArrayList<>();

    private static volatile Language language = Language.EN;

    public static void setLanguage(Language newLanguage) {
        if (newLanguage == null || newLanguage == language) {
            return;
        }
        language = newLanguage;
        List<Runnable> snapshot;
        synchronized (listeners) {
            snapshot = new ArrayList<>(listeners);
        }
        for (Runnable listener : snapshot) {
            try {
                listener.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static Language getLanguage() {
        return language;
    }

    public static void onLanguageChange(Runnable listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public static String get(String key, Object... args) {
        String pattern = (language == Language.DE ? DE : EN).get(key);
        if (pattern == null) {
            pattern = EN.get(key);
        }
        if (pattern == null) {
            return "!" + key;
        }
        if (args == null || args.length == 0) {
            return pattern;
        }
        try {
            return String.format(Locale.ROOT, pattern, args);
        } catch (Throwable t) {
            return pattern;
        }
    }

    static void put(String key, String en, String de) {
        EN.put(key, en);
        DE.put(key, de);
    }

    public static int size() {
        return EN.size();
    }

    static {
        MessageTable.fill();
    }
}
