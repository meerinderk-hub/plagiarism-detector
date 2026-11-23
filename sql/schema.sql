-- Run once:  mysql -u root -p < sql/schema.sql
CREATE DATABASE IF NOT EXISTS plagiarism_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE plagiarism_db;

CREATE TABLE IF NOT EXISTS documents (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  title       VARCHAR(200) NOT NULL,
  content     MEDIUMTEXT   NOT NULL,
  word_count  INT          NOT NULL,
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- One row per "Compare" click (a batch of 2+ documents).
CREATE TABLE IF NOT EXISTS comparison_runs (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  doc_count          INT        NOT NULL,
  ignore_stop_words  TINYINT(1) NOT NULL DEFAULT 0,
  created_at         TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- One row per document pair inside a run.
-- Deleting a run or a document automatically removes the related reports.
CREATE TABLE IF NOT EXISTS comparison_reports (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id         BIGINT       NOT NULL,
  doc_a_id       BIGINT       NOT NULL,
  doc_b_id       BIGINT       NOT NULL,
  words_a        INT          NOT NULL,
  words_b        INT          NOT NULL,
  common_unique  INT          NOT NULL,   -- number of distinct shared words
  overlap_words  INT          NOT NULL,   -- sum of min(freqA, freqB)
  similarity     DECIMAL(5,2) NOT NULL,   -- percentage 0-100
  common_words   MEDIUMTEXT   NOT NULL,   -- "word:count,word:count,..."
  FOREIGN KEY (run_id)   REFERENCES comparison_runs(id) ON DELETE CASCADE,
  FOREIGN KEY (doc_a_id) REFERENCES documents(id)       ON DELETE CASCADE,
  FOREIGN KEY (doc_b_id) REFERENCES documents(id)       ON DELETE CASCADE
);
