package br.edu.ufrgs.model;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class InventoryFileReader {
    private static final Pattern PT_BR_NUMBER = Pattern.compile("-?\\d+(,\\d+)?");
    private static final String[] EXPECTED_HEADER = {
            "id", "produto", "categoria", "data_validade", "preco_custo"
    };

    private final CsvFileReader csvFileReader;

    public InventoryFileReader() {
        this(new CsvFileReader());
    }

    InventoryFileReader(CsvFileReader csvFileReader) {
        this.csvFileReader = csvFileReader;
    }

    public List<Product> loadInventory(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new RuntimeException("Inventory resource path cannot be empty");
        }

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new RuntimeException("Inventory file not found: " + resourcePath);
        }

        return loadInventory(inputStream);
    }

    public List<Product> loadInventory(InputStream inputStream) {
        if (inputStream == null) {
            throw new RuntimeException("Inventory input stream cannot be null");
        }

        List<CsvFileReader.CsvRow> rows = csvFileReader.readRows(inputStream);
        if (rows.isEmpty()) {
            throw new RuntimeException("Inventory file is empty");
        }

        validateHeader(rows.get(0).fields());

        List<Product> products = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            products.add(parseProduct(rows.get(i)));
        }

        if (products.isEmpty()) {
            throw new RuntimeException("Inventory file does not contain products");
        }

        return products;
    }

    private static void validateHeader(List<String> fields) {
        if (fields.size() != EXPECTED_HEADER.length) {
            throw new RuntimeException("Invalid inventory header");
        }

        for (int i = 0; i < EXPECTED_HEADER.length; i++) {
            if (!EXPECTED_HEADER[i].equals(fields.get(i))) {
                throw new RuntimeException("Invalid inventory header");
            }
        }
    }

    private static Product parseProduct(CsvFileReader.CsvRow row) {
        List<String> fields = row.fields();
        int lineNumber = row.lineNumber();

        if (fields.size() != EXPECTED_HEADER.length) {
            throw new RuntimeException("Invalid inventory row at line " + lineNumber);
        }

        int id = parseId(requiredField(fields.get(0), "id", lineNumber), lineNumber);
        String productName = requiredField(fields.get(1), "produto", lineNumber);
        String category = requiredField(fields.get(2), "categoria", lineNumber);
        LocalDate expiryDate = parseDate(requiredField(fields.get(3), "data_validade", lineNumber), lineNumber);
        double priceCost = parsePriceCost(requiredField(fields.get(4), "preco_custo", lineNumber), lineNumber);

        return new Product(id, productName, category, expiryDate, priceCost);
    }

    private static String requiredField(String value, String fieldName, int lineNumber) {
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new RuntimeException("Field " + fieldName + " cannot be empty at line " + lineNumber);
        }
        return trimmedValue;
    }

    private static int parseId(String value, int lineNumber) {
        try {
            int id = Integer.parseInt(value);
            if (id < 0) {
                throw new RuntimeException("Field id cannot be negative at line " + lineNumber);
            }
            return id;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Field id must be a valid integer at line " + lineNumber, e);
        }
    }

    private static LocalDate parseDate(String value, int lineNumber) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Field data_validade must use yyyy-MM-dd at line " + lineNumber, e);
        }
    }

    private static double parsePriceCost(String value, int lineNumber) {
        double priceCost = parsePtBrNumber(value, "preco_custo", lineNumber);
        if (priceCost < 0) {
            throw new RuntimeException("Field preco_custo cannot be negative at line " + lineNumber);
        }
        return priceCost;
    }

    private static double parsePtBrNumber(String value, String fieldName, int lineNumber) {
        if (!PT_BR_NUMBER.matcher(value).matches()) {
            throw new RuntimeException("Field " + fieldName + " must be a valid PT-BR number at line " + lineNumber);
        }

        try {
            double number = Double.parseDouble(value.replace(',', '.'));
            if (!Double.isFinite(number)) {
                throw new RuntimeException("Field " + fieldName + " must be finite at line " + lineNumber);
            }
            return number;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Field " + fieldName + " must be a valid PT-BR number at line " + lineNumber, e);
        }
    }
}
