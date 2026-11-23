package com.plagiarism.model;

import java.sql.Timestamp;

public record RunSummary(long id, Timestamp createdAt, int docCount, double maxSimilarity) { }
