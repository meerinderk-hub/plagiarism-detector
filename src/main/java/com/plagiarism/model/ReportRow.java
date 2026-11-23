package com.plagiarism.model;

public record ReportRow(String titleA, String titleB, int wordsA, int wordsB, int commonUnique,
                        int overlap, double similarity, String commonWords) { }
