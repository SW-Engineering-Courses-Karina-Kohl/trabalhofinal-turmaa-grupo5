package br.edu.ufrgs.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServletDownloadTest {

    private static final String CSV =
            "id;produto;categoria;data_validade;preco_custo;status_estoque;prejuizo_estimado\n"
            + "502;Presunto;Frios;2026-03-30;32,00;VENCIDO;32,00";

    private ServletDownload servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @BeforeEach
    public void setUp() {
        servlet = new ServletDownload();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
    }

    @Test
    @DisplayName("Streams the CSV from the session as a downloadable file")
    public void testDoGetWithCsvStreamsDownload() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(ServletMedia.SESSION_KEY_CSV)).thenReturn(CSV);

        StringWriter buffer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(buffer));

        servlet.doGet(request, response);

        verify(response).setContentType("text/csv; charset=UTF-8");
        verify(response).setHeader("Content-Disposition", "attachment; filename=\"auditoria_estoque.csv\"");
        verify(response).setCharacterEncoding("UTF-8");
        assertEquals(CSV, buffer.toString());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("Redirects to index when there is no session")
    public void testDoGetWithoutSessionRedirects() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/index.jsp");
        verify(response, never()).getWriter();
    }

    @Test
    @DisplayName("Redirects to index when the session has no exported CSV")
    public void testDoGetWithoutCsvRedirects() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(ServletMedia.SESSION_KEY_CSV)).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/index.jsp");
        verify(response, never()).getWriter();
    }
}
