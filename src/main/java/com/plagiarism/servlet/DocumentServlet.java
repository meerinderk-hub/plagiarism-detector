package com.plagiarism.servlet;

import com.plagiarism.dao.DocumentDao;
import com.plagiarism.dao.RunDao;
import com.plagiarism.model.StoredDocument;
import com.plagiarism.util.InputValidator;
import com.plagiarism.view.HtmlPages;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Manages stored documents.
 *   GET  /documents          list
 *   GET  /documents?id=N     view one
 *   POST /documents          action=rename (id, title) or action=delete (id)
 */
@WebServlet("/documents")
public class DocumentServlet extends HttpServlet {

    private final DocumentDao documentDao = new DocumentDao();
    private final RunDao runDao = new RunDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ctx = req.getContextPath();
        String idParam = req.getParameter("id");
        try {
            if (idParam == null) {
                ServletUtil.html(resp, 200, HtmlPages.documentsPage(ctx, documentDao.findAll()));
                return;
            }
            Long id = InputValidator.parseId(idParam);
            if (id == null) {
                ServletUtil.html(resp, 400, HtmlPages.errorPage(ctx, "Invalid document id."));
                return;
            }
            StoredDocument doc = documentDao.findById(id);
            if (doc == null) {
                ServletUtil.html(resp, 404, HtmlPages.errorPage(ctx, "Document #" + id + " not found."));
            } else {
                ServletUtil.html(resp, 200, HtmlPages.documentPage(ctx, doc));
            }
        } catch (SQLException e) {
            ServletUtil.html(resp, 500, HtmlPages.errorPage(ctx, "Database error: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String ctx = req.getContextPath();
        String action = req.getParameter("action");
        Long id = InputValidator.parseId(req.getParameter("id"));
        if (id == null || action == null) {
            ServletUtil.html(resp, 400, HtmlPages.errorPage(ctx, "Invalid request."));
            return;
        }
        try {
            if ("rename".equals(action)) {
                String title = InputValidator.cleanTitle(req.getParameter("title"), "Untitled");
                documentDao.updateTitle(id, title);
                resp.sendRedirect(ctx + "/documents?id=" + id);
            } else if ("delete".equals(action)) {
                documentDao.delete(id);
                runDao.deleteEmptyRuns();
                resp.sendRedirect(ctx + "/documents");
            } else {
                ServletUtil.html(resp, 400, HtmlPages.errorPage(ctx, "Unknown action."));
            }
        } catch (SQLException e) {
            ServletUtil.html(resp, 500, HtmlPages.errorPage(ctx, "Database error: " + e.getMessage()));
        }
    }
}
