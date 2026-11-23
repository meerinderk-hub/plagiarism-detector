package com.plagiarism.util;

import com.plagiarism.model.Doc;
import com.plagiarism.model.PairRecord;
import com.plagiarism.model.PairResult;

import java.util.*;

/**
 * Similarity formula (word-frequency overlap):
 *
 *   overlap    = sum over every word w of  min(freqA(w), freqB(w))
 *   similarity = 2 * overlap / (totalWordsA + totalWordsB) * 100
 *
 * Identical documents give 100%; documents with no shared words give 0%.
 */
public final class SimilarityCalculator {

    private SimilarityCalculator() { }

    public static PairResult compare(Map<String, Integer> a, Map<String, Integer> b) {
        int totalA = a.values().stream().mapToInt(Integer::intValue).sum();
        int totalB = b.values().stream().mapToInt(Integer::intValue).sum();

        Map<String, Integer> common = new HashMap<>();
        int overlap = 0;
        for (Map.Entry<String, Integer> e : a.entrySet()) {
            Integer other = b.get(e.getKey());
            if (other != null) {
                int shared = Math.min(e.getValue(), other);
                common.put(e.getKey(), shared);
                overlap += shared;
            }
        }

        double similarity = (totalA + totalB) == 0 ? 0.0 : 200.0 * overlap / (totalA + totalB);
        similarity = Math.round(similarity * 100.0) / 100.0;

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(common.entrySet());
        sorted.sort(Map.Entry.<String, Integer>comparingByValue().reversed()
                .thenComparing(Map.Entry.comparingByKey()));
        Map<String, Integer> ordered = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : sorted) ordered.put(e.getKey(), e.getValue());

        return new PairResult(totalA, totalB, a.size(), b.size(), overlap, similarity, ordered);
    }

    /** Compares every pair of documents (n documents give n(n-1)/2 pairs). */
    public static List<PairRecord> compareAll(List<Doc> docs) {
        List<PairRecord> pairs = new ArrayList<>();
        for (int i = 0; i < docs.size(); i++) {
            for (int j = i + 1; j < docs.size(); j++) {
                pairs.add(new PairRecord(i, j,
                        compare(docs.get(i).frequencies(), docs.get(j).frequencies())));
            }
        }
        return pairs;
    }
}
