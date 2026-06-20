package br.edu.ufrgs.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CsvFileReaderTest {
    @Test
    @DisplayName("Reads semicolon-delimited CSV rows")
    public void testReadRowsSuccess() {
        CsvFileReader reader = new CsvFileReader();
        String csv = """
                id;produto
                501;Mussarela
                """;

        List<CsvFileReader.CsvRow> rows = reader.readRows(inputStream(csv));

        assertEquals(2, rows.size());
        assertEquals(1, rows.get(0).lineNumber());
        assertEquals(List.of("id", "produto"), rows.get(0).fields());
        assertEquals(2, rows.get(1).lineNumber());
        assertEquals(List.of("501", "Mussarela"), rows.get(1).fields());
    }

    @Test
    @DisplayName("Reads quoted fields with semicolons and escaped quotes")
    public void testReadRowsQuotedFields() {
        CsvFileReader reader = new CsvFileReader();
        String csv = "id;produto;categoria\n"
                + "501;\"Molho; Tomate\";\"Mercearia \"\"Seca\"\"\"\n";

        List<CsvFileReader.CsvRow> rows = reader.readRows(inputStream(csv));

        assertEquals("Molho; Tomate", rows.get(1).fields().get(1));
        assertEquals("Mercearia \"Seca\"", rows.get(1).fields().get(2));
    }

    @Test
    @DisplayName("Skips blank lines while preserving real line numbers")
    public void testReadRowsSkipsBlankLines() {
        CsvFileReader reader = new CsvFileReader();
        String csv = """
                id;produto

                501;Mussarela
                """;

        List<CsvFileReader.CsvRow> rows = reader.readRows(inputStream(csv));

        assertEquals(2, rows.size());
        assertEquals(3, rows.get(1).lineNumber());
    }

    @Test
    @DisplayName("Throws when a CSV row has malformed quotes")
    public void testReadRowsMalformedQuotes() {
        CsvFileReader reader = new CsvFileReader();
        String csv = """
                id;produto
                501;"Mussarela
                """;

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reader.readRows(inputStream(csv));
        });

        assertTrue(exception.getMessage().contains("Malformed CSV row at line 2"));
    }

    @Test
    @DisplayName("Throws when the CSV resource does not exist")
    public void testReadRowsMissingFile() {
        CsvFileReader reader = new CsvFileReader();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reader.readRows("test/non_existent_inventory.csv");
        });

        assertTrue(exception.getMessage().contains("CSV file not found"));
    }

    private static ByteArrayInputStream inputStream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
