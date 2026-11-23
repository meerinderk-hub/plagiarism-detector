package com.plagiarism.dao;

import com.plagiarism.model.Doc;
import com.plagiarism.model.StoredDocument;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD for the documents table. */
public class DocumentDao {

    /** CREATE - uses the caller's connection so it can join a transaction. */
    public long insert(Connection c, Doc d) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO documents (title, content, word_count) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.title());
            ps.setString(2, d.text());
            ps.setInt(3, d.wordCount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key returned");
                return keys.getLong(1);
            }
        }
    }

    /** READ (list) - content is left out to keep the list light. */
    public List<StoredDocument> findAll() throws SQLException {
        List<StoredDocument> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT id, title, word_count, created_at FROM documents ORDER BY id DESC LIMIT 200");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new StoredDocument(rs.getLong("id"), rs.getString("title"), null,
                        rs.getInt("word_count"), rs.getTimestamp("created_at")));
            }
        }
        return out;
    }

    /** READ (one) - returns null when the id does not exist. */
    public StoredDocument findById(long id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT id, title, content, word_count, created_at FROM documents WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new StoredDocument(rs.getLong("id"), rs.getString("title"), rs.getString("content"),
                        rs.getInt("word_count"), rs.getTimestamp("created_at"));
            }
        }
    }

    /** UPDATE - renames a document. */
    public boolean updateTitle(long id, String title) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("UPDATE documents SET title = ? WHERE id = ?")) {
            ps.setString(1, title);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** DELETE - related reports are removed by the foreign key (ON DELETE CASCADE). */
    public boolean delete(long id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM documents WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
