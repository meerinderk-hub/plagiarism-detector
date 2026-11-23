package com.plagiarism.model;

/** A comparison result tied to the positions of the two documents in the submitted list. */
public record PairRecord(int indexA, int indexB, PairResult result) { }
