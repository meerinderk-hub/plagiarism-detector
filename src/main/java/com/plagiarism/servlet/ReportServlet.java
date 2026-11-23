package com.plagiarism.servlet;

import com.plagiarism.dao.RunDao;
import com.plagiarism.model.RunDetail;
import com.plagiarism.util.InputValidator;
import com.plagiarism.view.HtmlPages;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** GET /report?id=N - shows one saved comparison report. */
@WebServlet("/report")
public class ReportServlet extends HttpServlet {

    private final RunDao runDao = new RunDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ctx = req.getContextPath();
        Long id = InputValidator.parseId(req.getParameter("id"));
        if (id == null) {
            ServletUtil.html(resp, 400, HtmlPages.errorPage(ctx, "Missing or invalid report id."));
            return;
        }
        try {
            RunDetail run = runDao.getRun(id);
            if (run == null) {
                ServletUtil.html(resp, 404, HtmlPages.errorPage(ctx, "Report #" + id + " not found."));
            } else {
                ServletUtil.html(resp, 200, HtmlPages.reportPage(ctx, run));
            }
        } catch (SQLException e) {
            ServletUtil.html(resp, 500, HtmlPages.errorPage(ctx, "Database error: " + e.getMessage()));
        }
    }
}
