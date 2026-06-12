package br.edu.ufrgs.controller;

import br.edu.ufrgs.model.ConfigFileReader;
import br.edu.ufrgs.model.DiscardParameter;
import br.edu.ufrgs.model.Inventory;
import br.edu.ufrgs.model.InventoryFileReader;
import br.edu.ufrgs.model.InventoryFileWriter;
import br.edu.ufrgs.model.Product;
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Clear any previous CSV export from the session before processing
        HttpSession session = request.getSession();
        session.removeAttribute(SESSION_KEY_CSV);

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
            //        status_estoque + prejuizo_estimado) and keep it in the session
            //        so the user can download it via /download (ServletDownload)
            InventoryFileWriter inventoryFileWriter = new InventoryFileWriter();
            String csvExportado = inventoryFileWriter.export(produtos);
            session.setAttribute(SESSION_KEY_CSV, csvExportado);

            // RF03 - Pass the product list to the JSP for colour-coded table rendering
            request.setAttribute("produtos", produtos);

        } catch (RuntimeException e) {
            request.setAttribute("erro", e.getMessage());
        }

        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
