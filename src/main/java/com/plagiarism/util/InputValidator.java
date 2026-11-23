package com.plagiarism.util;

/** Validation of everything that arrives from the browser. */
public final class InputValidator {

    public static final int MAX_DOCS = 10;
    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MAX_TEXT_CHARS = 1_000_000;

    private InputValidator() { }

    public static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    public static boolean textTooLong(String s) {
        return s != null && s.length() > MAX_TEXT_CHARS;
    }

    /** Trims the title, applies a fallback when empty and cuts it to the maximum length. */
    public static String cleanTitle(String title, String fallback) {
        String t = title == null ? "" : title.trim();
        if (t.isEmpty()) t = fallback;
        return t.length() > MAX_TITLE_LENGTH ? t.substring(0, MAX_TITLE_LENGTH) : t;
    }

    /** Returns the id, or null when the value is missing or not a positive number. */
    public static Long parseId(String s) {
        if (s == null) return null;
        try {
            long id = Long.parseLong(s.trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
