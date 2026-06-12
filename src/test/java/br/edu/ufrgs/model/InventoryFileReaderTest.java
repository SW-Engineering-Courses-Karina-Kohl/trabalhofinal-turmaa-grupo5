package br.edu.ufrgs.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InventoryFileReaderTest {
    @Test
    @DisplayName("Loads all products from a valid PT-BR inventory file")
    public void testLoadInventorySuccess() {
        InventoryFileReader reader = new InventoryFileReader();

        List<Product> products = reader.loadInventory("test/inventory_valid_test.csv");

        assertEquals(3, products.size());

        Product firstProduct = products.get(0);
        assertEquals(501, firstProduct.getId());
        assertEquals("Mussarela", firstProduct.getProdName());
        assertEquals("Laticinios", firstProduct.getCategory());
        assertEquals(LocalDate.of(2026, 4, 5), firstProduct.getExpiryDate());
        assertEquals(45.00, firstProduct.getPriceCost());
        assertNull(firstProduct.getStockStatus());
        assertEquals(0.0, firstProduct.getPredictedLoss());
    }

    @Test
    @DisplayName("Loads quoted PT-BR inventory fields")
    public void testLoadInventoryQuotedFields() {
        InventoryFileReader reader = new InventoryFileReader();
        String csv = "id;produto;categoria;data_validade;preco_custo\n"
                + "501;\"Molho; Tomate\";\"Mercearia \"\"Seca\"\"\";2026-04-05;45,50\n";

        List<Product> products = reader.loadInventory(inputStream(csv));

        assertEquals(1, products.size());
        assertEquals("Molho; Tomate", products.get(0).getProdName());
        assertEquals("Mercearia \"Seca\"", products.get(0).getCategory());
        assertEquals(45.50, products.get(0).getPriceCost());
    }

    @Test
    @DisplayName("Throws when the inventory file does not exist")
    public void testLoadInventoryMissingFile() {
        InventoryFileReader reader = new InventoryFileReader();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reader.loadInventory("test/non_existent_inventory.csv");
        });

        assertTrue(exception.getMessage().contains("Inventory file not found"));
    }

    @Test
    @DisplayName("Throws when the inventory file is empty")
    public void testLoadInventoryEmptyFile() {
        InventoryFileReader reader = new InventoryFileReader();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reader.loadInventory(new ByteArrayInputStream(new byte[0]));
        });

        assertTrue(exception.getMessage().contains("Inventory file is empty"));
    }

    @Test
    @DisplayName("Throws when the inventory header columns are invalid")
    public void testLoadInventoryInvalidHeader() {
        assertInventoryError("test/inventory_invalid_header_test.csv", "Invalid inventory header");
    }

    @Test
    @DisplayName("Throws when the file has a header but no products")
    public void testLoadInventoryHeaderOnly() {
        assertInventoryError("test/inventory_header_only_test.csv", "Inventory file does not contain products");
    }

    @Test
    @DisplayName("Throws when an inventory required field is empty")
    public void testLoadInventoryEmptyField() {
        assertInventoryError("test/inventory_empty_field_test.csv", "Field produto cannot be empty at line 2");
    }

    @Test
    @DisplayName("Throws when an inventory row has the wrong number of columns")
    public void testLoadInventoryInvalidRowLength() {
        assertInventoryError("test/inventory_invalid_row_length_test.csv", "Invalid inventory row at line 2");
    }

    @Test
    @DisplayName("Throws when the id is not a valid integer")
    public void testLoadInventoryInvalidId() {
        assertInventoryError("test/inventory_invalid_id_test.csv", "Field id must be a valid integer at line 2");
    }

    @Test
    @DisplayName("Throws when the id is negative")
    public void testLoadInventoryNegativeId() {
        assertInventoryError("test/inventory_negative_id_test.csv", "Field id cannot be negative at line 2");
    }

    @Test
    @DisplayName("Throws when the expiry date is not in yyyy-MM-dd format")
    public void testLoadInventoryInvalidDate() {
        assertInventoryError("test/inventory_invalid_date_test.csv", "Field data_validade must use yyyy-MM-dd at line 2");
    }

    @Test
    @DisplayName("Throws when the price is not a valid PT-BR number")
    public void testLoadInventoryInvalidPriceCost() {
        assertInventoryError("test/inventory_invalid_price_test.csv", "Field preco_custo must be a valid PT-BR number at line 2");
    }

    @Test
    @DisplayName("Throws when the price uses dot decimal format")
    public void testLoadInventoryRejectsDotDecimalPriceCost() {
        InventoryFileReader reader = new InventoryFileReader();
        String csv = """
                id;produto;categoria;data_validade;preco_custo
                501;Mussarela;Laticinios;2026-04-05;45.00
                """;

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reader.loadInventory(inputStream(csv));
        });

        assertTrue(exception.getMessage().contains("Field preco_custo must be a valid PT-BR number at line 2"));
    }

    @Test
    @DisplayName("Throws when the price is negative")
    public void testLoadInventoryNegativePriceCost() {
        assertInventoryError("test/inventory_negative_price_test.csv", "Field preco_custo cannot be negative at line 2");
    }

    private static void assertInventoryError(String resourcePath, String expectedMessage) {
        InventoryFileReader reader = new InventoryFileReader();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reader.loadInventory(resourcePath);
        });

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    private static ByteArrayInputStream inputStream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
