package com.plagiarism.model;

import java.util.Map;

/**
 * Result of comparing two documents.
 *
 * @param overlap      sum over shared words of min(freqA, freqB)
 * @param similarity   percentage 0-100
 * @param commonWords  shared word -> min(freqA, freqB), sorted by count desc then alphabetically
 */
public record PairResult(int wordsA, int wordsB, int uniqueA, int uniqueB,
                         int overlap, double similarity, Map<String, Integer> commonWords) { }
