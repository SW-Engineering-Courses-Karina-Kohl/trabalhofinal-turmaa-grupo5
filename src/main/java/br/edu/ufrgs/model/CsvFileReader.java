package br.edu.ufrgs.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvFileReader {
    private static final char DELIMITER = ';';
    private static final char QUOTE = '"';

    public List<CsvRow> readRows(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new RuntimeException("CSV resource path cannot be empty");
        }

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new RuntimeException("CSV file not found: " + resourcePath);
        }

        return readRows(inputStream);
    }

    public List<CsvRow> readRows(InputStream inputStream) {
        if (inputStream == null) {
            throw new RuntimeException("CSV input stream cannot be null");
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<CsvRow> rows = new ArrayList<>();
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                rows.add(new CsvRow(lineNumber, parseLine(line, lineNumber)));
            }

            return rows;
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        }
    }

    private static List<String> parseLine(String line, int lineNumber) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean justClosedQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);

            if (inQuotes) {
                if (currentChar == QUOTE) {
                    if (i + 1 < line.length() && line.charAt(i + 1) == QUOTE) {
                        current.append(QUOTE);
                        i++;
                    } else {
                        inQuotes = false;
                        justClosedQuote = true;
                    }
                } else {
                    current.append(currentChar);
                }
                continue;
            }

            if (justClosedQuote) {
                if (currentChar == DELIMITER) {
                    fields.add(current.toString().trim());
                    current.setLength(0);
                    justClosedQuote = false;
                } else if (!Character.isWhitespace(currentChar)) {
                    throw new RuntimeException("Malformed CSV row at line " + lineNumber);
                }
                continue;
            }

            if (currentChar == DELIMITER) {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else if (currentChar == QUOTE) {
                if (!current.toString().isBlank()) {
                    throw new RuntimeException("Malformed CSV row at line " + lineNumber);
                }
                current.setLength(0);
                inQuotes = true;
            } else {
                current.append(currentChar);
            }
        }

        if (inQuotes) {
            throw new RuntimeException("Malformed CSV row at line " + lineNumber);
        }

        fields.add(current.toString().trim());
        return fields;
    }

    public record CsvRow(int lineNumber, List<String> fields) {
        public CsvRow {
            fields = List.copyOf(fields);
        }
    }
}
