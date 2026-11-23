package com.plagiarism.model;

import java.util.Map;

/** A submitted document after processing (before it is stored). */
public record Doc(String title, String text, Map<String, Integer> frequencies, int wordCount) { }
