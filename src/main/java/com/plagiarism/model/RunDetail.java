package com.plagiarism.model;

import java.sql.Timestamp;
import java.util.List;

public record RunDetail(long id, Timestamp createdAt, int docCount, boolean ignoreStopWords,
                        List<ReportRow> rows) { }
