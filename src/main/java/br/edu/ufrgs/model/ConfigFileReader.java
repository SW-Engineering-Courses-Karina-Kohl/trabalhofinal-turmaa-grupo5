package br.edu.ufrgs.model;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

public class ConfigFileReader {
    private static final String DEFAULT_CONFIG_RESOURCE_PATH = "config_alimentos.csv";
    private static final Pattern PT_BR_NUMBER = Pattern.compile("-?\\d+(,\\d+)?");
    private static final String[] EXPECTED_HEADER = {
            "parametro", "valor"
    };

    private final CsvFileReader csvFileReader;

    public ConfigFileReader() {
        this(new CsvFileReader());
    }

    ConfigFileReader(CsvFileReader csvFileReader) {
        this.csvFileReader = csvFileReader;
    }

    public DiscardParameter loadDiscardParameter(String resourcePath) {
        String resolvedPath = resourcePath;
        if (resolvedPath == null || resolvedPath.isBlank()) {
            resolvedPath = DEFAULT_CONFIG_RESOURCE_PATH;
        }

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resolvedPath);
        if (inputStream == null) {
            throw new RuntimeException("Configuration file not found: " + resolvedPath);
        }

        return loadDiscardParameter(inputStream);
    }

    DiscardParameter loadDiscardParameter(InputStream inputStream) {
        if (inputStream == null) {
            throw new RuntimeException("Configuration input stream cannot be null");
        }

        List<CsvFileReader.CsvRow> rows = csvFileReader.readRows(inputStream);
        if (rows.isEmpty()) {
            throw new RuntimeException("Configuration file is empty");
        }

        validateHeader(rows.get(0).fields());

        DiscardParameter discardParameter = new DiscardParameter();
        boolean foundMarginOfSafetyDays = false;
        boolean foundDiscardFactorPercentage = false;

        for (int i = 1; i < rows.size(); i++) {
            CsvFileReader.CsvRow row = rows.get(i);
            List<String> fields = row.fields();
            int lineNumber = row.lineNumber();

            if (fields.size() != EXPECTED_HEADER.length) {
                throw new RuntimeException("Invalid configuration row at line " + lineNumber);
            }

            String parameter = requiredField(fields.get(0), "parametro", lineNumber);
            String value = requiredField(fields.get(1), "valor", lineNumber);

            if ("margem_seguranca_dias".equals(parameter)) {
                discardParameter.setMarginOfSafetyDays(parseMarginOfSafetyDays(value, lineNumber));
                foundMarginOfSafetyDays = true;
            } else if ("fator_descarte_percentual".equals(parameter)) {
                discardParameter.setDiscardFactorPercentage(parseDiscardFactorPercentage(value, lineNumber));
                foundDiscardFactorPercentage = true;
            } else {
                throw new RuntimeException("Unknown parameter at line " + lineNumber + ": " + parameter);
            }
        }

        if (!foundMarginOfSafetyDays) {
            throw new RuntimeException("Missing configuration parameter: margem_seguranca_dias");
        }
        if (!foundDiscardFactorPercentage) {
            throw new RuntimeException("Missing configuration parameter: fator_descarte_percentual");
        }

        return discardParameter;
    }

    private static void validateHeader(List<String> fields) {
        if (fields.size() != EXPECTED_HEADER.length) {
            throw new RuntimeException("Invalid configuration header");
        }

        for (int i = 0; i < EXPECTED_HEADER.length; i++) {
            if (!EXPECTED_HEADER[i].equals(fields.get(i))) {
                throw new RuntimeException("Invalid configuration header");
            }
        }
    }

    private static String requiredField(String value, String fieldName, int lineNumber) {
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new RuntimeException("Field " + fieldName + " cannot be empty at line " + lineNumber);
        }
        return trimmedValue;
    }

    private static int parseMarginOfSafetyDays(String value, int lineNumber) {
        try {
            int marginOfSafetyDays = Integer.parseInt(value);
            if (marginOfSafetyDays < 0) {
                throw new RuntimeException("Field valor cannot be negative at line " + lineNumber);
            }
            return marginOfSafetyDays;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Field valor must be a valid integer at line " + lineNumber, e);
        }
    }

    private static double parseDiscardFactorPercentage(String value, int lineNumber) {
        double discardFactorPercentage = parsePtBrNumber(value, "valor", lineNumber);
        if (discardFactorPercentage < 0) {
            throw new RuntimeException("Field valor cannot be negative at line " + lineNumber);
        }
        return discardFactorPercentage;
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
