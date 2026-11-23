package com.plagiarism.servlet;

import com.plagiarism.model.Doc;
import com.plagiarism.service.ComparisonService;
import com.plagiarism.util.InputValidator;
import com.plagiarism.util.TextProcessor;
import com.plagiarism.view.HtmlPages;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** POST /compare - validates the submitted documents, compares and stores them, then shows the report. */
@WebServlet("/compare")
public class CompareServlet extends HttpServlet {

    private final ComparisonService service = new ComparisonService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String ctx = req.getContextPath();
        boolean ignoreStopWords = "on".equals(req.getParameter("ignoreStopWords"));

        List<Doc> docs = new ArrayList<>();
        for (int i = 1; i <= InputValidator.MAX_DOCS; i++) {
            String text = req.getParameter("text" + i);
            if (!InputValidator.hasText(text)) continue;
            if (InputValidator.textTooLong(text)) {
                ServletUtil.html(resp, 400, HtmlPages.errorPage(ctx,
                    "Document " + i + " is too long (limit " + InputValidator.MAX_TEXT_CHARS + " characters)."));
                return;
            }
            List<String> tokens = TextProcessor.tokenize(text, ignoreStopWords);
            if (tokens.isEmpty()) continue;
            String title = InputValidator.cleanTitle(req.getParameter("title" + i), "Document " + i);
            docs.add(new Doc(title, text, TextProcessor.frequencies(tokens), tokens.size()));
        }

        if (docs.size() < 2) {
            ServletUtil.html(resp, 400, HtmlPages.errorPage(ctx,
                "Please provide at least two documents that contain real words."));
            return;
        }

        try {
            long runId = service.compareAndSave(docs, ignoreStopWords);
            resp.sendRedirect(ctx + "/report?id=" + runId);
        } catch (SQLException e) {
            ServletUtil.html(resp, 500, HtmlPages.errorPage(ctx,
                "Could not save to MySQL. Check that the database is running, schema.sql has been run, and "
                + "DB_URL / DB_USER / DB_PASSWORD are correct. (" + e.getMessage() + ")"));
        }
    }
}
