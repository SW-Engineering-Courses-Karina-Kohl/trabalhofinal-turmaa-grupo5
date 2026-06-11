package br.edu.ufrgs.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InventoryFileWriterTest {

    private static final String HEADER =
            "id,produto,categoria,data_validade,preco_custo,status_estoque,prejuizo_estimado";

    // Builds a new product with status and predicted loss already set for testing
    private Product buildProduct(int id, String name, String category, String expiryDate, double priceCost, StockStatus status, double predictedLoss) {
        Product product = new Product(id, name, category, LocalDate.parse(expiryDate), priceCost);
        product.setStockStatus(status);
        product.setPredictedLoss(predictedLoss);
        return product;
    }

    @Test
    @DisplayName("Empty product list exports only the CSV header")
    public void testEmptyListReturnsOnlyHeader() {
        String csv = new InventoryFileWriter().export(List.of());

        assertEquals(HEADER, csv);
    }

    @Test
    @DisplayName("Expired product exports the status as VENCIDO")
    public void testExpiredStatusLabel() {
        Product product = buildProduct(1, "Presunto", "Frios", "2026-03-30", 32.00, StockStatus.EXPIRED, 32.00);

        String csv = new InventoryFileWriter().export(List.of(product));

        assertEquals(HEADER + "\n1,Presunto,Frios,2026-03-30,32.00,VENCIDO,32.00", csv);
    }

    @Test
    @DisplayName("Product near expiration date exports the status as ALERTA")
    public void testAlertStatusLabel() {
        Product product = buildProduct(2, "Mussarela", "Laticinios", "2026-04-05", 45.00, StockStatus.ALERT, 0.00);

        String csv = new InventoryFileWriter().export(List.of(product));

        assertEquals(HEADER + "\n2,Mussarela,Laticinios,2026-04-05,45.00,ALERTA,0.00", csv);
    }

    @Test
    @DisplayName("Valid product exports the status as OK")
    public void testOkStatusLabel() {
        Product product = buildProduct(3, "Molho de Tomate", "Mercearia", "2026-06-15", 10.00, StockStatus.OK, 0.00);

        String csv = new InventoryFileWriter().export(List.of(product));

        assertEquals(HEADER + "\n3,Molho de Tomate,Mercearia,2026-06-15,10.00,OK,0.00", csv);
    }

    @Test
    @DisplayName("Monetary values are formatted with a dot and two decimals")
    public void testAmountIsFormattedWithDotAndTwoDecimals() {
        Product product = buildProduct(4, "Item", "Cat", "2026-01-01", 5, StockStatus.EXPIRED, 5);

        String csv = new InventoryFileWriter().export(List.of(product));

        assertEquals(HEADER + "\n4,Item,Cat,2026-01-01,5.00,VENCIDO,5.00", csv);
    }

    @Test
    @DisplayName("Multiple products are exported one per line after the header")
    public void testMultipleProductsAreSeparatedByNewline() {
        List<Product> products = List.of(
                buildProduct(501, "Mussarela", "Laticinios", "2026-04-05", 45.00, StockStatus.ALERT, 0.00),
                buildProduct(502, "Presunto", "Frios", "2026-03-30", 32.00, StockStatus.EXPIRED, 32.00),
                buildProduct(503, "Molho de Tomate", "Mercearia", "2026-06-15", 10.00, StockStatus.OK, 0.00)
        );

        String csv = new InventoryFileWriter().export(products);

        String expected = HEADER + "\n"
                + "501,Mussarela,Laticinios,2026-04-05,45.00,ALERTA,0.00\n"
                + "502,Presunto,Frios,2026-03-30,32.00,VENCIDO,32.00\n"
                + "503,Molho de Tomate,Mercearia,2026-06-15,10.00,OK,0.00";

        assertEquals(expected, csv);
    }
}
