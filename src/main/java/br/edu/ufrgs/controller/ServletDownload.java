package br.edu.ufrgs.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/download")
public class ServletDownload extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String csvExportado = (session != null)
                ? (String) session.getAttribute(ServletMedia.SESSION_KEY_CSV)
                : null;

        // Guard: no CSV in session means the user hasn't processed an inventory yet
        if (csvExportado == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        // RF04 - Stream the generated CSV as a downloadable file
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"auditoria_estoque.csv\"");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.print(csvExportado);
        }
    }
}
