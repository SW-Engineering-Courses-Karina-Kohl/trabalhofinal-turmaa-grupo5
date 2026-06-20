package br.edu.ufrgs.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigFileReaderTest {
    @Test
    @DisplayName("Loads discard parameters from a valid PT-BR config file")
    public void testLoadDiscardParameterSuccess() {
        ConfigFileReader reader = new ConfigFileReader();

        DiscardParameter discardParameter = reader.loadDiscardParameter("test/config_file_valid_test.csv");

        assertEquals(3, discardParameter.getMarginOfSafetyDays());
        assertEquals(1.0, discardParameter.getDiscardFactorPercentage());
    }

    @Test
    @DisplayName("Throws when the config file does not exist")
    public void testLoadDiscardParameterMissingFile() {
        ConfigFileReader reader = new ConfigFileReader();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reader.loadDiscardParameter("non_existent_file.csv");
        });

        assertTrue(exception.getMessage().contains("Configuration file not found"));
    }

    @Test
    @DisplayName("Throws when a config value is empty")
    public void testLoadDiscardParameterEmptyField() {
        assertConfigError("test/config_file_empty_field_test.csv", "Field valor cannot be empty at line 2");
    }

    @Test
    @DisplayName("Throws when discard factor is negative")
    public void testLoadDiscardParameterInvalidContent() {
        assertConfigError("test/config_file_invalid_field_test.csv", "Field valor cannot be negative at line 3");
    }

    @Test
    @DisplayName("Throws when discard factor uses dot decimal format")
    public void testLoadDiscardParameterRejectsDotDecimal() {
        ConfigFileReader reader = new ConfigFileReader();
        String csv = """
                parametro;valor
                margem_seguranca_dias;3
                fator_descarte_percentual;1.0
                """;

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reader.loadDiscardParameter(inputStream(csv));
        });

        assertTrue(exception.getMessage().contains("Field valor must be a valid PT-BR number at line 3"));
    }

    private static void assertConfigError(String resourcePath, String expectedMessage) {
        ConfigFileReader reader = new ConfigFileReader();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reader.loadDiscardParameter(resourcePath);
        });

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    private static ByteArrayInputStream inputStream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
