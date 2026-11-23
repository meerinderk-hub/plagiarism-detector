package com.plagiarism.service;

import com.plagiarism.dao.DBConnection;
import com.plagiarism.dao.DocumentDao;
import com.plagiarism.dao.RunDao;
import com.plagiarism.model.Doc;
import com.plagiarism.model.PairRecord;
import com.plagiarism.util.SimilarityCalculator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Business logic: compare the documents and save everything in one transaction. */
public class ComparisonService {

    private final DocumentDao documentDao = new DocumentDao();
    private final RunDao runDao = new RunDao();

    /** Returns the id of the saved run. */
    public long compareAndSave(List<Doc> docs, boolean ignoreStopWords) throws SQLException {
        List<PairRecord> pairs = SimilarityCalculator.compareAll(docs);

        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                long[] docIds = new long[docs.size()];
                for (int i = 0; i < docs.size(); i++) {
                    docIds[i] = documentDao.insert(c, docs.get(i));
                }
                long runId = runDao.insertRun(c, docs.size(), ignoreStopWords);
                runDao.insertReports(c, runId, docIds, pairs);
                c.commit();
                return runId;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
