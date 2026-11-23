package com.plagiarism.util;

import java.util.*;

/** Text normalization, tokenization and word-frequency counting. */
public final class TextProcessor {

    private static final Set<String> STOP_WORDS = Set.of(
        "a", "an", "the", "and", "or", "but", "if", "of", "to", "in", "on", "at", "by", "for",
        "with", "from", "as", "is", "are", "was", "were", "be", "been", "being", "it", "its",
        "this", "that", "these", "those", "i", "you", "he", "she", "we", "they", "them", "his",
        "her", "our", "their", "not", "no", "so", "than", "then", "there", "have", "has", "had",
        "do", "does", "did", "will", "would", "can", "could", "should", "may", "might", "about",
        "into", "over", "also", "such");

    private TextProcessor() { }

    /** Lower-cases the text and strips punctuation, leaving only letters, digits and single spaces. */
    public static String normalize(String text) {
        if (text == null) return "";
        String s = text.toLowerCase(Locale.ROOT);
        s = s.replaceAll("['\u2019]", "");              // don't -> dont
        s = s.replaceAll("[^\\p{L}\\p{N}\\s]", " ");    // remove punctuation and symbols
        return s.replaceAll("\\s+", " ").trim();
    }

    /** Normalizes and splits text into words, optionally dropping common stop words. */
    public static List<String> tokenize(String text, boolean removeStopWords) {
        String normalized = normalize(text);
        if (normalized.isEmpty()) return List.of();
        List<String> tokens = new ArrayList<>();
        for (String word : normalized.split(" ")) {
            if (removeStopWords && STOP_WORDS.contains(word)) continue;
            tokens.add(word);
        }
        return tokens;
    }

    /** Counts how many times each word occurs (HashMap word -> count). */
    public static Map<String, Integer> frequencies(List<String> tokens) {
        Map<String, Integer> freq = new HashMap<>();
        for (String t : tokens) freq.merge(t, 1, Integer::sum);
        return freq;
    }
}
