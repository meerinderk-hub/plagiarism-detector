package com.plagiarism.model;

import java.sql.Timestamp;

/** A document row from the database. content is null in list views. */
public record StoredDocument(long id, String title, String content, int wordCount, Timestamp createdAt) { }
