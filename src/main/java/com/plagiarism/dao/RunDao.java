package com.plagiarism.dao;

import com.plagiarism.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** CRUD for comparison_runs and comparison_reports. */
public class RunDao {

    private static final int MAX_STORED_COMMON_WORDS = 300;

    /** CREATE a run - uses the caller's connection so it can join a transaction. */
    public long insertRun(Connection c, int docCount, boolean ignoreStopWords) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO comparison_runs (doc_count, ignore_stop_words) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, docCount);
            ps.setBoolean(2, ignoreStopWords);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key returned");
                return keys.getLong(1);
            }
        }
    }

    /** CREATE the pair reports of a run in one batch. */
    public void insertReports(Connection c, long runId, long[] docIds, List<PairRecord> pairs) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO comparison_reports (run_id, doc_a_id, doc_b_id, words_a, words_b, "
                + "common_unique, overlap_words, similarity, common_words) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            for (PairRecord p : pairs) {
                PairResult r = p.result();
                ps.setLong(1, runId);
                ps.setLong(2, docIds[p.indexA()]);
                ps.setLong(3, docIds[p.indexB()]);
                ps.setInt(4, r.wordsA());
                ps.setInt(5, r.wordsB());
                ps.setInt(6, r.commonWords().size());
                ps.setInt(7, r.overlap());
                ps.setBigDecimal(8, BigDecimal.valueOf(r.similarity()));
                ps.setString(9, serialize(r.commonWords()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** READ (list) - newest runs first with their highest similarity. */
    public List<RunSummary> listRuns() throws SQLException {
        String sql = "SELECT r.id, r.created_at, r.doc_count, COALESCE(MAX(p.similarity), 0) AS top "
                   + "FROM comparison_runs r LEFT JOIN comparison_reports p ON p.run_id = r.id "
                   + "GROUP BY r.id, r.created_at, r.doc_count ORDER BY r.id DESC LIMIT 50";
        List<RunSummary> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new RunSummary(rs.getLong("id"), rs.getTimestamp("created_at"),
                        rs.getInt("doc_count"), rs.getDouble("top")));
            }
        }
        return out;
    }

    /** READ (one) - returns null when the run does not exist. */
    public RunDetail getRun(long runId) throws SQLException {
        try (Connection c = DBConnection.get()) {
            long id;
            Timestamp created;
            int docCount;
            boolean ignore;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id, created_at, doc_count, ignore_stop_words FROM comparison_runs WHERE id = ?")) {
                ps.setLong(1, runId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    id = rs.getLong("id");
                    created = rs.getTimestamp("created_at");
                    docCount = rs.getInt("doc_count");
                    ignore = rs.getBoolean("ignore_stop_words");
                }
            }

            List<ReportRow> rows = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT da.title AS ta, db.title AS tb, p.words_a, p.words_b, p.common_unique, "
                    + "p.overlap_words, p.similarity, p.common_words "
                    + "FROM comparison_reports p "
                    + "JOIN documents da ON da.id = p.doc_a_id "
                    + "JOIN documents db ON db.id = p.doc_b_id "
                    + "WHERE p.run_id = ? ORDER BY p.similarity DESC, p.id")) {
                ps.setLong(1, runId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new ReportRow(rs.getString("ta"), rs.getString("tb"),
                                rs.getInt("words_a"), rs.getInt("words_b"), rs.getInt("common_unique"),
                                rs.getInt("overlap_words"), rs.getDouble("similarity"),
                                rs.getString("common_words")));
                    }
                }
            }
            return new RunDetail(id, created, docCount, ignore, rows);
        }
    }

    /** DELETE a run - its pair reports go with it (ON DELETE CASCADE). */
    public boolean deleteRun(long runId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM comparison_runs WHERE id = ?")) {
            ps.setLong(1, runId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Removes runs that lost all their reports because their documents were deleted. */
    public int deleteEmptyRuns() throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM comparison_runs WHERE id NOT IN (SELECT run_id FROM comparison_reports)")) {
            return ps.executeUpdate();
        }
    }

    /** "word:count,word:count" - safe because words only contain letters and digits. */
    private static String serialize(Map<String, Integer> common) {
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (Map.Entry<String, Integer> e : common.entrySet()) {
            if (n++ >= MAX_STORED_COMMON_WORDS) break;
            if (sb.length() > 0) sb.append(',');
            sb.append(e.getKey()).append(':').append(e.getValue());
        }
        return sb.toString();
    }
}
