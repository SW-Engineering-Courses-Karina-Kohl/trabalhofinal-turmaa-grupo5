<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="br.edu.ufrgs.model.Product" %>
<%@ page import="br.edu.ufrgs.model.StockStatus" %>

<%!
    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATE_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String statusLabel(StockStatus status) {
        if (status == StockStatus.EXPIRED) return "VENCIDO";
        if (status == StockStatus.ALERT) return "ALERTA";
        return "OK";
    }

    private String rowClass(StockStatus status) {
        if (status == StockStatus.EXPIRED) return "bg-red-100";
        if (status == StockStatus.ALERT) return "bg-yellow-100";
        return "";
    }

    private String badgeClass(StockStatus status) {
        if (status == StockStatus.EXPIRED) return "bg-red-200 text-red-800";
        if (status == StockStatus.ALERT) return "bg-yellow-200 text-yellow-800";
        return "bg-green-200 text-green-800";
    }

    private String money(double value) {
        return String.format(BR, "R$ %.2f", value);
    }

    private String date(LocalDate value) {
        return value.format(DATE_BR);
    }
%>

<%
    // MOCK TEMPORARIO --------------------------------------
    if (request.getParameter("preview") != null
            && request.getAttribute("produtos") == null && request.getAttribute("erro") == null) {
        List<Product> mock = new ArrayList<>();

        Product m1 = new Product(501, "Mussarela", "Laticinios", LocalDate.parse("2026-04-05"), 45.00);
        m1.setStockStatus(StockStatus.EXPIRED);
        m1.setPredictedLoss(45.00);
        mock.add(m1);

        Product m2 = new Product(502, "Presunto", "Frios", LocalDate.parse("2026-06-14"), 32.00);
        m2.setStockStatus(StockStatus.ALERT);
        m2.setPredictedLoss(0.00);
        mock.add(m2);

        Product m3 = new Product(503, "Molho de Tomate", "Mercearia", LocalDate.parse("2026-12-15"), 10.00);
        m3.setStockStatus(StockStatus.OK);
        m3.setPredictedLoss(0.00);
        mock.add(m3);

        request.setAttribute("produtos", mock);
    }
    // -------------------------------------------------------

    @SuppressWarnings("unchecked")
    List<Product> products = (List<Product>) request.getAttribute("produtos");
    String error = (String) request.getAttribute("erro");

    int totalExpired = 0;
    int totalAlerts = 0;
    double totalLoss = 0.0;
    if (products != null) {
        for (Product p : products) {
            if (p.getStockStatus() == StockStatus.EXPIRED) totalExpired++;
            else if (p.getStockStatus() == StockStatus.ALERT) totalAlerts++;
            totalLoss += p.getPredictedLoss();
        }
    }
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Auditoria de Desperdício de Estoque</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&amp;display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined&amp;display=swap" rel="stylesheet"/>
    <style>
        body { font-family: "Inter", sans-serif; }
    </style>
</head>
<body class="bg-gray-50 text-gray-900 min-h-screen flex flex-col">

    <!-- Header -->
    <header class="bg-white border-b border-gray-200 top-0 z-50">
        <div class="flex items-center h-16 w-full max-w-[1000px] mx-auto px-4 md:px-0">
            <span class="text-xl font-bold text-blue-800">FoodWaste</span>
        </div>
    </header>

    <div class="flex flex-1 max-w-[1000px] mx-auto w-full px-4 md:px-0 py-6">
        <main class="flex-1 flex flex-col gap-6">

            <!-- Title -->
            <section class="flex flex-col gap-2">
                <h1 class="text-3xl font-bold">Auditoria de Desperdício e Validade</h1>
                <p class="text-base text-gray-600">Identifique produtos próximos do vencimento e calcule o prejuízo potencial.</p>
            </section>

            <!-- ERROR -->
            <% if (error != null) { %>
            <div class="bg-red-100 text-red-800 p-4 rounded-lg border border-red-300 flex items-center gap-2">
                <span class="material-symbols-outlined">error</span>
                <span class="text-sm"><%= error %></span>
            </div>
            <% } %>

            <!-- Upload -->
            <section class="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                <form action="processa" class="flex flex-col gap-4" enctype="multipart/form-data" method="post">
                    <div>
                        <label class="block text-sm font-semibold mb-2" for="inventario">Selecione o arquivo de inventário (.csv)</label>
                        <div class="flex gap-4 items-center">
                            <input accept=".csv" required
                                   class="block w-full text-sm text-gray-600 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-blue-100 file:text-blue-800 hover:file:bg-blue-200 cursor-pointer border border-gray-300 rounded-lg p-2"
                                   id="inventario" name="inventario" type="file"/>
                            <button class="bg-blue-800 text-white px-6 py-2 rounded-lg text-sm font-semibold hover:bg-blue-900 transition-colors whitespace-nowrap" type="submit">Auditar</button>
                        </div>
                        <p class="mt-2 text-sm text-gray-500">O arquivo deve conter: id, produto, categoria, data_validade, preco_custo</p>
                    </div>
                </form>
            </section>

            <!-- KPIS -->
            <section class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div class="bg-white rounded-lg border border-gray-200 p-6 shadow-sm flex flex-col gap-2">
                    <div class="flex items-center gap-2 text-red-600">
                        <span class="material-symbols-outlined">warning</span>
                        <span class="text-sm font-semibold">Itens Vencidos</span>
                    </div>
                    <span class="text-3xl font-bold"><%= totalExpired %></span>
                </div>
                <div class="bg-white rounded-lg border border-gray-200 p-6 shadow-sm flex flex-col gap-2">
                    <div class="flex items-center gap-2 text-yellow-600">
                        <span class="material-symbols-outlined">schedule</span>
                        <span class="text-sm font-semibold">Itens em Alerta</span>
                    </div>
                    <span class="text-3xl font-bold"><%= totalAlerts %></span>
                </div>
                <div class="bg-white rounded-lg border border-gray-200 p-6 shadow-sm flex flex-col gap-2">
                    <div class="flex items-center gap-2 text-gray-800">
                        <span class="material-symbols-outlined">attach_money</span>
                        <span class="text-sm font-semibold">Prejuízo Total Estimado</span>
                    </div>
                    <span class="text-3xl font-bold text-red-600"><%= money(totalLoss) %></span>
                </div>
            </section>

            <% if (products != null && !products.isEmpty()) { %>

            <!-- Table -->
            <section class="flex flex-col gap-4">
                <div class="flex justify-between items-center">
                    <h2 class="text-xl font-semibold">Resultado da Auditoria</h2>
                    <%-- RF04: depende de um endpoint de download no servlet (ex.: GET /exporta) --%>
                    <a href="exporta" class="border border-gray-400 text-gray-800 px-4 py-2 rounded-lg text-sm font-semibold hover:bg-gray-100 transition-colors flex items-center gap-2">
                        <span class="material-symbols-outlined text-sm">download</span>
                        Exportar CSV
                    </a>
                </div>
                <div class="bg-white rounded-lg border border-gray-200 overflow-hidden shadow-sm">
                    <div class="overflow-x-auto">
                        <table class="w-full text-left border-collapse">
                            <thead>
                                <tr class="bg-gray-100 text-gray-700 text-xs font-semibold uppercase tracking-wider">
                                    <th class="px-4 py-3 border-b border-gray-200">ID</th>
                                    <th class="px-4 py-3 border-b border-gray-200">Produto</th>
                                    <th class="px-4 py-3 border-b border-gray-200">Categoria</th>
                                    <th class="px-4 py-3 border-b border-gray-200">Validade</th>
                                    <th class="px-4 py-3 border-b border-gray-200">Preço de Custo</th>
                                    <th class="px-4 py-3 border-b border-gray-200">Status</th>
                                    <th class="px-4 py-3 border-b border-gray-200 text-right">Prejuízo Estimado</th>
                                </tr>
                            </thead>
                            <tbody class="text-sm divide-y divide-gray-200">
                                <% for (Product p : products) {
                                       StockStatus status = p.getStockStatus(); %>
                                <tr class="<%= rowClass(status) %>">
                                    <td class="px-4 py-3 text-gray-600"><%= p.getId() %></td>
                                    <td class="px-4 py-3 font-medium"><%= p.getProdName() %></td>
                                    <td class="px-4 py-3 text-gray-600"><%= p.getCategory() %></td>
                                    <td class="px-4 py-3 text-gray-600"><%= date(p.getExpiryDate()) %></td>
                                    <td class="px-4 py-3 text-gray-600"><%= money(p.getPriceCost()) %></td>
                                    <td class="px-4 py-3">
                                        <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold <%= badgeClass(status) %>"><%= statusLabel(status) %></span>
                                    </td>
                                    <td class="px-4 py-3 text-right font-medium"><%= money(p.getPredictedLoss()) %></td>
                                </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>

            <% } else if (error == null) { %>

            <!-- Empty State -->
            <div class="bg-white rounded-lg border border-gray-200 flex flex-col items-center justify-center text-center py-16 gap-4">
                <span class="material-symbols-outlined text-5xl text-gray-400">upload_file</span>
                <p class="text-base text-gray-600">Envie um arquivo de inventário para iniciar a auditoria</p>
            </div>

            <% } %>

        </main>
    </div>

    <!-- Footer -->
    <footer class="bg-white py-6 border-t border-gray-200 mt-auto">
        <div class="max-w-[1000px] mx-auto px-4 md:px-0 text-sm text-gray-500">
            <p>© 2026/1 FoodWaste - Sistema de Auditoria de Desperdício e Validade (INF01120 - Desenvolvimento De Software - Turma A - Grupo 5)</p>
        </div>
    </footer>

</body>
</html>
