package br.edu.ufrgs.controller;

import br.edu.ufrgs.model.ConfigFileReader;
import br.edu.ufrgs.model.DiscardParameter;
import br.edu.ufrgs.model.Inventory;
import br.edu.ufrgs.model.InventoryFileReader;
import br.edu.ufrgs.model.InventoryFileWriter;
import br.edu.ufrgs.model.Product;
import br.edu.ufrgs.model.StockStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@WebServlet("/processa")
@MultipartConfig
public class ServletMedia extends HttpServlet {

    // Session key used by ServletDownload to retrieve the generated CSV (RF04)
    public static final String SESSION_KEY_CSV = "csvExportado";
    public static final String SESSION_KEY_PRODUCTS = "produtos";
    public static final String SESSION_KEY_TOTAL_EXPIRED = "totalExpired";
    public static final String SESSION_KEY_TOTAL_ALERTS = "totalAlerts";
    public static final String SESSION_KEY_TOTAL_LOSS = "totalLoss";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        clearAuditResult(session);

        try {
            // RF01 - Receive the inventory CSV file selected by the user
            Part inventarioPart = request.getPart("inventario");
            if (inventarioPart == null || inventarioPart.getSize() == 0) {
                request.setAttribute("erro", "Nenhum arquivo de inventário foi enviado.");
                request.getRequestDispatcher("index.jsp").forward(request, response);
                return;
            }

            // RF02 - Load and validate the inventory from the uploaded file
            InventoryFileReader inventoryFileReader = new InventoryFileReader();
            List<Product> produtos;
            try (InputStream inventarioStream = inventarioPart.getInputStream()) {
                produtos = inventoryFileReader.loadInventory(inventarioStream);
            }

            // RF02 - Load the discard configuration (margin of safety and discard factor)
            //        from the external config file bundled with the application
            ConfigFileReader configFileReader = new ConfigFileReader();
            DiscardParameter config = configFileReader.loadDiscardParameter("config_alimentos.csv");

            // RF02 - Evaluate each product: assign StockStatus and calculate predicted loss
            Inventory inventory = new Inventory();
            inventory.evaluateInventory(produtos, config);

            // RF04 - Export the audited inventory to an updated CSV (original columns +
            //        status_estoque + prejuizo_estimado)
            InventoryFileWriter inventoryFileWriter = new InventoryFileWriter();
            String csvExportado = inventoryFileWriter.export(produtos);

            // RF03 - Pass the product list to the JSP for colour-coded table rendering
            //        and the CSV is served for download via
            //        /download (RF04 - ServletDownload).
            storeAuditResult(session, produtos, csvExportado);

        } catch (RuntimeException e) {
            request.setAttribute("erro", e.getMessage());
        }

        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    // Computes the summary indicators (KPIs) and stores the whole audit result in the session
    private void storeAuditResult(HttpSession session, List<Product> produtos, String csv) {
        int totalExpired = 0;
        int totalAlerts = 0;
        double totalLoss = 0.0;
        for (Product p : produtos) {
            if (p.getStockStatus() == StockStatus.EXPIRED) totalExpired++;
            else if (p.getStockStatus() == StockStatus.ALERT) totalAlerts++;
            totalLoss += p.getPredictedLoss();
        }

        session.setAttribute(SESSION_KEY_PRODUCTS, produtos);
        session.setAttribute(SESSION_KEY_CSV, csv);
        session.setAttribute(SESSION_KEY_TOTAL_EXPIRED, totalExpired);
        session.setAttribute(SESSION_KEY_TOTAL_ALERTS, totalAlerts);
        session.setAttribute(SESSION_KEY_TOTAL_LOSS, totalLoss);
    }

    // Removes any previous audit result from the session.
    private void clearAuditResult(HttpSession session) {
        session.removeAttribute(SESSION_KEY_PRODUCTS);
        session.removeAttribute(SESSION_KEY_CSV);
        session.removeAttribute(SESSION_KEY_TOTAL_EXPIRED);
        session.removeAttribute(SESSION_KEY_TOTAL_ALERTS);
        session.removeAttribute(SESSION_KEY_TOTAL_LOSS);
    }
}
