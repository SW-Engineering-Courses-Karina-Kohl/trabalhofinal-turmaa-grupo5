package br.edu.ufrgs.model;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InventoryFileReaderTest {
    @Test
    public void testInventoryFileReaderSuccess() {
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
    public void testInventoryFileReaderMissingFile() {
        InventoryFileReader reader = new InventoryFileReader();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reader.loadInventory("test/non_existent_inventory.csv");
        });

        assertTrue(exception.getMessage().contains("Inventory file not found"));
    }

    @Test
    public void testInventoryFileReaderEmptyFile() {
        InventoryFileReader reader = new InventoryFileReader();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reader.loadInventory(new ByteArrayInputStream(new byte[0]));
        });

        assertTrue(exception.getMessage().contains("Inventory file is empty"));
    }

    @Test
    public void testInventoryFileReaderInvalidHeader() {
        assertInventoryError("test/inventory_invalid_header_test.csv", "Invalid inventory header");
    }

    @Test
    public void testInventoryFileReaderHeaderOnly() {
        assertInventoryError("test/inventory_header_only_test.csv", "Inventory file does not contain products");
    }

    @Test
    public void testInventoryFileReaderEmptyField() {
        assertInventoryError("test/inventory_empty_field_test.csv", "Field produto cannot be empty at line 2");
    }

    @Test
    public void testInventoryFileReaderInvalidRowLength() {
        assertInventoryError("test/inventory_invalid_row_length_test.csv", "Invalid inventory row at line 2");
    }

    @Test
    public void testInventoryFileReaderInvalidId() {
        assertInventoryError("test/inventory_invalid_id_test.csv", "Field id must be a valid integer at line 2");
    }

    @Test
    public void testInventoryFileReaderNegativeId() {
        assertInventoryError("test/inventory_negative_id_test.csv", "Field id cannot be negative at line 2");
    }

    @Test
    public void testInventoryFileReaderInvalidDate() {
        assertInventoryError("test/inventory_invalid_date_test.csv", "Field data_validade must use yyyy-MM-dd at line 2");
    }

    @Test
    public void testInventoryFileReaderInvalidPriceCost() {
        assertInventoryError("test/inventory_invalid_price_test.csv", "Field preco_custo must be a valid number at line 2");
    }

    @Test
    public void testInventoryFileReaderNegativePriceCost() {
        assertInventoryError("test/inventory_negative_price_test.csv", "Field preco_custo cannot be negative at line 2");
    }

    private static void assertInventoryError(String resourcePath, String expectedMessage) {
        InventoryFileReader reader = new InventoryFileReader();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reader.loadInventory(resourcePath);
        });

        assertTrue(exception.getMessage().contains(expectedMessage));
    }
}
