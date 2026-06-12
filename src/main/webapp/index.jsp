<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="br.edu.ufrgs.model.Product" %>
<%@ page import="br.edu.ufrgs.model.StockStatus" %>
<%@ page import="java.util.ResourceBundle" %>

<%!
    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATE_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String statusLabel(StockStatus status, ResourceBundle msg) {
        if (status == StockStatus.EXPIRED) return msg.getString("status.expired");
        if (status == StockStatus.ALERT) return msg.getString("status.alert");
        return msg.getString("status.ok");
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
    // Resolução de idioma
    String langParam = request.getParameter("lang");
    if (langParam != null && (langParam.equals("pt") || langParam.equals("en") || langParam.equals("es"))) {
        session.setAttribute("lang", langParam);
    }
    String lang = (String) session.getAttribute("lang");
    if (lang == null) lang = "pt";
    ResourceBundle msg = ResourceBundle.getBundle("i18n.messages", new Locale(lang));

    @SuppressWarnings("unchecked")
    List<Product> products = (List<Product>) session.getAttribute("produtos");
    String error = (String) request.getAttribute("erro");

    Integer totalExpired = (Integer) session.getAttribute("totalExpired");
    Integer totalAlerts = (Integer) session.getAttribute("totalAlerts");
    Double totalLoss = (Double) session.getAttribute("totalLoss");
    if (totalExpired == null) totalExpired = 0;
    if (totalAlerts == null) totalAlerts = 0;
    if (totalLoss == null) totalLoss = 0.0;
%>
<!DOCTYPE html>
<html lang="<%= lang %>">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title><%= msg.getString("page.title") %></title>
    <script src="js/tailwind.js"></script>
    <style>
        body { font-family: system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; }
    </style>
</head>
<body class="bg-gray-50 text-gray-900 min-h-screen flex flex-col">

    <!-- Header -->
    <header class="bg-white border-b border-gray-200 top-0 z-50">
        <div class="flex items-center justify-between h-16 w-full max-w-[1000px] mx-auto px-4 md:px-0">
            <span class="text-xl font-bold text-blue-800">FoodWaste</span>
            <form method="get" action="index.jsp" class="flex items-center gap-2">
                <label for="lang" class="sr-only">Idioma</label>
                <svg class="w-5 h-5 text-gray-500" viewBox="0 0 24 24" fill="currentColor"><path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zm6.93 6h-2.95a15.65 15.65 0 0 0-1.38-3.56A8.03 8.03 0 0 1 18.92 8zM12 4.04c.83 1.2 1.48 2.53 1.91 3.96h-3.82c.43-1.43 1.08-2.76 1.91-3.96zM4.26 14C4.1 13.36 4 12.69 4 12s.1-1.36.26-2h3.38c-.08.66-.14 1.32-.14 2 0 .68.06 1.34.14 2H4.26zm.82 2h2.95c.32 1.25.78 2.45 1.38 3.56A7.99 7.99 0 0 1 5.08 16zm2.95-8H5.08a7.99 7.99 0 0 1 4.33-3.56A15.65 15.65 0 0 0 8.03 8zM12 19.96c-.83-1.2-1.48-2.53-1.91-3.96h3.82c-.43 1.43-1.08 2.76-1.91 3.96zM14.34 14H9.66c-.09-.66-.16-1.32-.16-2 0-.68.07-1.35.16-2h4.68c.09.65.16 1.32.16 2 0 .68-.07 1.34-.16 2zm.25 5.56c.6-1.11 1.06-2.31 1.38-3.56h2.95a8.03 8.03 0 0 1-4.33 3.56zM16.36 14c.08-.66.14-1.32.14-2 0-.68-.06-1.34-.14-2h3.38c.16.64.26 1.31.26 2s-.1 1.36-.26 2h-3.38z"/></svg>
                <select id="lang" name="lang" onchange="this.form.submit()"
                        class="border border-gray-300 rounded-lg text-sm px-2 py-1 bg-white cursor-pointer">
                    <option value="pt" <%= lang.equals("pt") ? "selected" : "" %>>Português</option>
                    <option value="en" <%= lang.equals("en") ? "selected" : "" %>>English</option>
                    <option value="es" <%= lang.equals("es") ? "selected" : "" %>>Español</option>
                </select>
            </form>
        </div>
    </header>

    <div class="flex flex-1 max-w-[1000px] mx-auto w-full px-4 md:px-0 py-6">
        <main class="flex-1 flex flex-col gap-6">

            <!-- Title -->
            <section class="flex flex-col gap-2">
                <h1 class="text-3xl font-bold"><%= msg.getString("header.title") %></h1>
                <p class="text-base text-gray-600"><%= msg.getString("header.subtitle") %></p>
            </section>

            <!-- ERROR -->
            <% if (error != null) { %>
            <div class="bg-red-100 text-red-800 p-4 rounded-lg border border-red-300 flex items-center gap-2">
                <svg class="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
                <span class="text-sm"><strong><%= msg.getString("error.label") %></strong> <%= error %></span>
            </div>
            <% } %>

            <!-- Upload -->
            <section class="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                <form action="processa" class="flex flex-col gap-4" enctype="multipart/form-data" method="post">
                    <div>
                        <label class="block text-sm font-semibold mb-2" for="inventario"><%= msg.getString("upload.label") %></label>
                        <div class="flex gap-4 items-center">
                            <input accept=".csv" required
                                   class="block w-full text-sm text-gray-600 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-blue-100 file:text-blue-800 hover:file:bg-blue-200 cursor-pointer border border-gray-300 rounded-lg p-2"
                                   id="inventario" name="inventario" type="file"/>
                            <button class="bg-blue-800 text-white px-6 py-2 rounded-lg text-sm font-semibold hover:bg-blue-900 transition-colors whitespace-nowrap" type="submit"><%= msg.getString("upload.button") %></button>
                        </div>
                        <p class="mt-2 text-sm text-gray-500"><%= msg.getString("upload.help") %></p>
                    </div>
                </form>
            </section>

            <!-- KPIS -->
            <section class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div class="bg-white rounded-lg border border-gray-200 p-6 shadow-sm flex flex-col gap-2">
                    <div class="flex items-center gap-2 text-red-600">
                        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/></svg>
                        <span class="text-sm font-semibold"><%= msg.getString("kpi.expired") %></span>
                    </div>
                    <span class="text-3xl font-bold"><%= totalExpired %></span>
                </div>
                <div class="bg-white rounded-lg border border-gray-200 p-6 shadow-sm flex flex-col gap-2">
                    <div class="flex items-center gap-2 text-yellow-600">
                        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor"><path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/></svg>
                        <span class="text-sm font-semibold"><%= msg.getString("kpi.alert") %></span>
                    </div>
                    <span class="text-3xl font-bold"><%= totalAlerts %></span>
                </div>
                <div class="bg-white rounded-lg border border-gray-200 p-6 shadow-sm flex flex-col gap-2">
                    <div class="flex items-center gap-2 text-gray-800">
                        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor"><path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/></svg>
                        <span class="text-sm font-semibold"><%= msg.getString("kpi.loss") %></span>
                    </div>
                    <span class="text-3xl font-bold text-red-600"><%= money(totalLoss) %></span>
                </div>
            </section>

            <% if (products != null && !products.isEmpty()) { %>

            <!-- Table -->
            <section class="flex flex-col gap-4">
                <div class="flex justify-between items-center">
                    <h2 class="text-xl font-semibold"><%= msg.getString("results.title") %></h2>
                    <a href="download" class="border border-gray-400 text-gray-800 px-4 py-2 rounded-lg text-sm font-semibold hover:bg-gray-100 transition-colors flex items-center gap-2">
                        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/></svg>
                        <%= msg.getString("results.export") %>
                    </a>
                </div>
                <div class="bg-white rounded-lg border border-gray-200 overflow-hidden shadow-sm">
                    <div class="overflow-x-auto">
                        <table class="w-full text-left border-collapse">
                            <thead>
                                <tr class="bg-gray-100 text-gray-700 text-xs font-semibold uppercase tracking-wider">
                                    <th class="px-4 py-3 border-b border-gray-200"><%= msg.getString("table.id") %></th>
                                    <th class="px-4 py-3 border-b border-gray-200"><%= msg.getString("table.product") %></th>
                                    <th class="px-4 py-3 border-b border-gray-200"><%= msg.getString("table.category") %></th>
                                    <th class="px-4 py-3 border-b border-gray-200"><%= msg.getString("table.expiry") %></th>
                                    <th class="px-4 py-3 border-b border-gray-200"><%= msg.getString("table.cost") %></th>
                                    <th class="px-4 py-3 border-b border-gray-200"><%= msg.getString("table.status") %></th>
                                    <th class="px-4 py-3 border-b border-gray-200 text-right"><%= msg.getString("table.loss") %></th>
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
                                        <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold <%= badgeClass(status) %>"><%= statusLabel(status, msg) %></span>
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
                <svg class="w-12 h-12 text-gray-400" viewBox="0 0 24 24" fill="currentColor"><path d="M9 16h6v-6h4l-7-7-7 7h4v6zm-4 2h14v2H5v-2z"/></svg>
                <p class="text-base text-gray-600"><%= msg.getString("empty.message") %></p>
            </div>

            <% } %>

        </main>
    </div>

    <!-- Footer -->
    <footer class="bg-white py-6 border-t border-gray-200 mt-auto">
        <div class="max-w-[1000px] mx-auto px-4 md:px-0 text-sm text-gray-500">
            <p><%= msg.getString("footer.text") %></p>
        </div>
    </footer>

</body>
</html>
