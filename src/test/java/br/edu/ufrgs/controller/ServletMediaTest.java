package br.edu.ufrgs.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServletMediaTest {

    // Inventory in the expected PT-BR format (semicolon separator, comma decimal)
    private static final String VALID_CSV =
            "id;produto;categoria;data_validade;preco_custo\n"
            + "501;Mussarela;Laticinios;2026-04-05;45,00\n"
            + "502;Presunto;Frios;2026-03-30;32,00\n"
            + "503;Molho de Tomate;Mercearia;2026-06-15;10,00\n";

    // Same data but with a dot decimal, which the PT-BR reader must reject
    private static final String INVALID_CSV =
            "id;produto;categoria;data_validade;preco_custo\n"
            + "501;Mussarela;Laticinios;2026-04-05;45.00\n";

    private ServletMedia servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    public void setUp() {
        servlet = new ServletMedia();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    // Builds a Part whose stream returns the given CSV content
    private Part csvPart(String content) throws Exception {
        Part part = mock(Part.class);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        when(part.getSize()).thenReturn((long) bytes.length);
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        return part;
    }

    @Test
    @DisplayName("Valid upload stores the audited products and CSV in the session")
    public void testDoPostValidInventoryStoresResultInSession() throws Exception {
        Part part = csvPart(VALID_CSV);
        when(request.getPart("inventario")).thenReturn(part);

        servlet.doPost(request, response);

        // Products list (3 items) is stored in the session for the JSP (RF03)
        ArgumentCaptor<Object> produtos = ArgumentCaptor.forClass(Object.class);
        verify(session).setAttribute(eq(ServletMedia.SESSION_KEY_PRODUCTS), produtos.capture());
        assertInstanceOf(List.class, produtos.getValue());
        assertEquals(3, ((List<?>) produtos.getValue()).size());

        // The exported CSV (RF04) is stored with the expected header
        ArgumentCaptor<Object> csv = ArgumentCaptor.forClass(Object.class);
        verify(session).setAttribute(eq(ServletMedia.SESSION_KEY_CSV), csv.capture());
        assertInstanceOf(String.class, csv.getValue());
        assertTrue(((String) csv.getValue())
                .startsWith("id;produto;categoria;data_validade;preco_custo;status_estoque;prejuizo_estimado"));

        // KPIs are stored as well
        verify(session).setAttribute(eq(ServletMedia.SESSION_KEY_TOTAL_EXPIRED), any());
        verify(session).setAttribute(eq(ServletMedia.SESSION_KEY_TOTAL_ALERTS), any());
        verify(session).setAttribute(eq(ServletMedia.SESSION_KEY_TOTAL_LOSS), any());

        // No error, and the request is forwarded back to the JSP
        verify(request, never()).setAttribute(eq("erro"), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Missing file sets an error message and does not store results")
    public void testDoPostWithoutFileSetsError() throws Exception {
        when(request.getPart("inventario")).thenReturn(null);

        servlet.doPost(request, response);

        verify(request).setAttribute("erro", "Nenhum arquivo de inventário foi enviado.");
        verify(session, never()).setAttribute(eq(ServletMedia.SESSION_KEY_PRODUCTS), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Empty file (size 0) sets an error message")
    public void testDoPostEmptyFileSetsError() throws Exception {
        Part emptyPart = mock(Part.class);
        when(emptyPart.getSize()).thenReturn(0L);
        when(request.getPart("inventario")).thenReturn(emptyPart);

        servlet.doPost(request, response);

        verify(request).setAttribute("erro", "Nenhum arquivo de inventário foi enviado.");
        verify(session, never()).setAttribute(eq(ServletMedia.SESSION_KEY_PRODUCTS), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Invalid CSV content is caught and reported as an error")
    public void testDoPostInvalidInventorySetsError() throws Exception {
        Part part = csvPart(INVALID_CSV);
        when(request.getPart("inventario")).thenReturn(part);

        servlet.doPost(request, response);

        // The RuntimeException from the reader is caught and surfaced via "erro"
        ArgumentCaptor<Object> erro = ArgumentCaptor.forClass(Object.class);
        verify(request).setAttribute(eq("erro"), erro.capture());
        assertInstanceOf(String.class, erro.getValue());
        assertTrue(((String) erro.getValue()).contains("preco_custo"));

        verify(session, never()).setAttribute(eq(ServletMedia.SESSION_KEY_PRODUCTS), any());
        verify(dispatcher).forward(request, response);
    }
}
