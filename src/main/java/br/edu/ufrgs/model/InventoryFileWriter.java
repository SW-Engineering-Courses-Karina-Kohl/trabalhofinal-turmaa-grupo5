package br.edu.ufrgs.model;

import java.util.List;
import java.util.Locale;

public class InventoryFileWriter {

    private static final String CSV_HEADER =
            "id,produto,categoria,data_validade,preco_custo,status_estoque,prejuizo_estimado";

    // Builds the full CSV and returns it as text.
    public String export(List<Product> produtos) {
        StringBuilder builder = new StringBuilder();
        builder.append(CSV_HEADER);
        for (Product produto : produtos) {
            builder.append("\n").append(toCsvRow(produto));
        }
        return builder.toString();
    }

    // Serializes a single product into a CSV row 
    private static String toCsvRow(Product produto) {
        return produto.getId() + "," +
                produto.getProdName() + "," +
                produto.getCategory() + "," +
                produto.getExpiryDate() + "," +
                formatAmount(produto.getPriceCost()) + "," +
                toStatusLabel(produto.getStockStatus()) + "," +
                formatAmount(produto.getPredictedLoss());
    }

    // Formats a monetary value (example: 32.00)
    private static String formatAmount(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }

    // Maps stock status enum to ptbr
    private static String toStatusLabel(StockStatus status) {
        switch (status) {
            case EXPIRED:
                return "VENCIDO";
            case ALERT:
                return "ALERTA";
            case OK:
                return "OK";
            default:
                throw new IllegalArgumentException("UNKNOWN STOCK STATUS: " + status);
        }
    }
}
