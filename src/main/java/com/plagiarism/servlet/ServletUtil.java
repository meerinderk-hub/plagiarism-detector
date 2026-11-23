package com.plagiarism.servlet;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

final class ServletUtil {

    private ServletUtil() { }

    static void html(HttpServletResponse resp, int status, String html) throws IOException {
        resp.setStatus(status);
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(html);
    }
}
