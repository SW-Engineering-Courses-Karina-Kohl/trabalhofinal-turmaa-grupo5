package br.edu.ufrgs.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InventoryTest {

    private static final DiscardParameter CONFIG = new DiscardParameter(3, 1.0);

    private Product buildProduct(LocalDate expiryDate) {
        return new Product(1, "Test Product", "Test Category", expiryDate, 32.00);
    }

    @Test
    @DisplayName("An expired product is marked EXPIRED with the predicted loss")
    public void testExpiredProduct() {
        Product product = buildProduct(LocalDate.now().minusDays(1));
        new Inventory().evaluateProduct(product, CONFIG);

        assertEquals(StockStatus.EXPIRED, product.getStockStatus());
        assertEquals(32.00, product.getPredictedLoss());
    }

    @Test
    @DisplayName("A product within the safety margin is marked ALERT with no loss")
    public void testAlertProduct() {
        Product product = buildProduct(LocalDate.now().plusDays(2));
        new Inventory().evaluateProduct(product, CONFIG);

        assertEquals(StockStatus.ALERT, product.getStockStatus());
        assertEquals(0.0, product.getPredictedLoss());
    }

    @Test
    @DisplayName("A product far from expiry is marked OK with no loss")
    public void testOkProduct() {
        Product product = buildProduct(LocalDate.now().plusDays(10));
        new Inventory().evaluateProduct(product, CONFIG);

        assertEquals(StockStatus.OK, product.getStockStatus());
        assertEquals(0.0, product.getPredictedLoss());
    }

    @Test
    @DisplayName("A product expiring today is marked ALERT with no loss")
    public void testProductExpiresToday() {
        Product product = buildProduct(LocalDate.now());
        new Inventory().evaluateProduct(product, CONFIG);

        assertEquals(StockStatus.ALERT, product.getStockStatus());
        assertEquals(0.0, product.getPredictedLoss());
    }

    @Test
    @DisplayName("Evaluating the inventory updates the status of every product")
    public void testEvaluateInventoryUpdatesAllProducts() {
        Inventory inventory = new Inventory();
        java.util.List<Product> products = java.util.List.of(
            buildProduct(LocalDate.now().minusDays(1)),
            buildProduct(LocalDate.now().plusDays(2)),
            buildProduct(LocalDate.now().plusDays(10))
        );

        inventory.evaluateInventory(products, CONFIG);

        assertEquals(StockStatus.EXPIRED, products.get(0).getStockStatus());
        assertEquals(StockStatus.ALERT, products.get(1).getStockStatus());
        assertEquals(StockStatus.OK, products.get(2).getStockStatus());
    }
}
