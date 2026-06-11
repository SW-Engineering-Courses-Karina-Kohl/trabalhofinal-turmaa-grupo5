package br.edu.ufrgs.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class InventoryFileReader {
    private static final String[] EXPECTED_HEADER = {
            "id", "produto", "categoria", "data_validade", "preco_custo"
    };

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

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            validateHeader(header);

            List<Product> products = new ArrayList<>();
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                products.add(parseProduct(line, lineNumber));
            }

            if (products.isEmpty()) {
                throw new RuntimeException("Inventory file does not contain products");
            }

            return products;
        } catch (IOException e) {
            throw new RuntimeException("Error reading inventory file: " + e.getMessage(), e);
        }
    }

    private static void validateHeader(String header) {
        if (header == null) {
            throw new RuntimeException("Inventory file is empty");
        }

        String[] fields = header.split(",", -1);
        if (fields.length != EXPECTED_HEADER.length) {
            throw new RuntimeException("Invalid inventory header");
        }

        for (int i = 0; i < EXPECTED_HEADER.length; i++) {
            if (!EXPECTED_HEADER[i].equals(fields[i].trim())) {
                throw new RuntimeException("Invalid inventory header");
            }
        }
    }

    private static Product parseProduct(String line, int lineNumber) {
        String[] fields = line.split(",", -1);
        if (fields.length != EXPECTED_HEADER.length) {
            throw new RuntimeException("Invalid inventory row at line " + lineNumber);
        }

        int id = parseId(requiredField(fields[0], "id", lineNumber), lineNumber);
        String productName = requiredField(fields[1], "produto", lineNumber);
        String category = requiredField(fields[2], "categoria", lineNumber);
        LocalDate expiryDate = parseDate(requiredField(fields[3], "data_validade", lineNumber), lineNumber);
        double priceCost = parsePriceCost(requiredField(fields[4], "preco_custo", lineNumber), lineNumber);

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
        try {
            double priceCost = Double.parseDouble(value);
            if (!Double.isFinite(priceCost)) {
                throw new RuntimeException("Field preco_custo must be finite at line " + lineNumber);
            }
            if (priceCost < 0) {
                throw new RuntimeException("Field preco_custo cannot be negative at line " + lineNumber);
            }
            return priceCost;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Field preco_custo must be a valid number at line " + lineNumber, e);
        }
    }
}
