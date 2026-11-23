package com.plagiarism.servlet;

import com.plagiarism.dao.RunDao;
import com.plagiarism.util.InputValidator;
import com.plagiarism.view.HtmlPages;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** GET /reports lists saved reports; POST /reports (action=delete) removes one. */
@WebServlet("/reports")
public class HistoryServlet extends HttpServlet {

    private final RunDao runDao = new RunDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ctx = req.getContextPath();
        try {
            ServletUtil.html(resp, 200, HtmlPages.historyPage(ctx, runDao.listRuns()));
        } catch (SQLException e) {
            ServletUtil.html(resp, 500, HtmlPages.errorPage(ctx, "Database error: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String ctx = req.getContextPath();
        Long id = InputValidator.parseId(req.getParameter("id"));
        if (!"delete".equals(req.getParameter("action")) || id == null) {
            ServletUtil.html(resp, 400, HtmlPages.errorPage(ctx, "Invalid request."));
            return;
        }
        try {
            runDao.deleteRun(id);
            resp.sendRedirect(ctx + "/reports");
        } catch (SQLException e) {
            ServletUtil.html(resp, 500, HtmlPages.errorPage(ctx, "Database error: " + e.getMessage()));
        }
    }
}
