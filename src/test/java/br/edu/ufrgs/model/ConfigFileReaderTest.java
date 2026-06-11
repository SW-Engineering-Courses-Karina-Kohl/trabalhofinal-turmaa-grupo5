package br.edu.ufrgs.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigFileReaderTest {
    @Test
    @DisplayName("Loads discard parameters from a valid config file")
    public void testConfigFileReaderSuccess() {
        ConfigFileReader configReader = new ConfigFileReader();
        DiscardParameter discardParameter = configReader.loadDiscardParameter("test/config_file_valid_test.csv"); 
        
        String expectedMessage = "DiscardParameter{marginOfSafetyDays=3, discardFactorPercentage=1.0}";
        String message = discardParameter.toString();
        assertTrue(message.contains(expectedMessage));
    }


    @Test
    @DisplayName("Throws when the config file does not exist")
    public void testConfigFileReaderMissingFile() {
        ConfigFileReader configReader = new ConfigFileReader();
        Exception exception = assertThrows(RuntimeException.class, () -> {
            configReader.loadDiscardParameter("non_existent_file.csv");
        });
        String expectedMessage = "Error reading configuration file";
        String message = exception.getMessage();
        assertTrue(message.contains(expectedMessage));
    }       

    @Test
    @DisplayName("Throws when margem_seguranca_dias has an empty value")
    public void testConfigFileReaderEmptyField() {
        ConfigFileReader configReader = new ConfigFileReader();
        Exception exception = assertThrows(RuntimeException.class, () -> {
            configReader.loadDiscardParameter("test/config_file_empty_field_test.csv");
        });
        System.out.println(exception.getMessage());
        String expectedMessage = "Value for margem_seguranca_dias cannot be empty";
        String message = exception.getMessage();
        assertTrue(message.contains(expectedMessage));
    }

    @Test
    @DisplayName("Throws when fator_descarte_percentual is empty or negative")
    public void testConfigFileReaderInvalidContent() {
        ConfigFileReader configReader = new ConfigFileReader();
        Exception exception = assertThrows(RuntimeException.class, () -> {
            configReader.loadDiscardParameter("test/config_file_invalid_field_test.csv");
        });
        System.out.println(exception.getMessage());
        String expectedMessage = "Value for fator_descarte_percentual cannot be empty or negative";
        String message = exception.getMessage();
        assertTrue(message.contains(expectedMessage));
    }
}
