<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Auditoria de Desperdício - FoodWaste</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        h2 { color: #333; }
        .form-section { margin-bottom: 24px; }
        input[type="file"] { margin: 8px 0; }
        button { padding: 8px 16px; cursor: pointer; }
    </style>
</head>
<body>
    <h2>Auditoria de Desperdício de Estoque</h2>

    <div class="form-section">
        <form action="processa" method="post" enctype="multipart/form-data">
            <label for="inventario">Selecione o arquivo de inventário (.csv):</label><br>
            <input type="file" id="inventario" name="inventario" accept=".csv" required><br><br>
            <button type="submit">Auditar</button>
        </form>
    </div>

    <%-- Tabela de resultados, preenchida pelo servlet após o processamento --%>
    <% if (request.getAttribute("produtos") != null) { %>
        <hr>
        <h3>Resultado da Auditoria</h3>
        <table border="1" cellpadding="8" cellspacing="0">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Produto</th>
                    <th>Categoria</th>
                    <th>Validade</th>
                    <th>Preço de Custo</th>
                    <th>Status</th>
                    <th>Prejuízo Estimado</th>
                </tr>
            </thead>
            <tbody>
                <%
                    java.util.List<br.edu.ufrgs.model.Product> produtos =
                        (java.util.List<br.edu.ufrgs.model.Product>) request.getAttribute("produtos");
                    for (br.edu.ufrgs.model.Product p : produtos) {
                        String status = p.getStockStatus().toString();
                        String cor = "";
                        if ("EXPIRED".equals(status)) cor = "background-color: #ff4d4d;";
                        else if ("ALERT".equals(status)) cor = "background-color: #ffff66;";
                %>
                <tr style="<%= cor %>">
                    <td><%= p.getId() %></td>
                    <td><%= p.getProdName() %></td>
                    <td><%= p.getCategory() %></td>
                    <td><%= p.getExpiryDate() %></td>
                    <td><%= String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.2f", p.getPriceCost()) %></td>
                    <td><%= status %></td>
                    <td><%= String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.2f", p.getPredictedLoss()) %></td>
                </tr>
                <% } %>
            </tbody>
        </table>
    <% } %>

    <% if (request.getAttribute("erro") != null) { %>
        <hr>
        <p style="color: red;"><strong>Erro: </strong><%= request.getAttribute("erro") %></p>
    <% } %>
</body>
</html>
