package br.edu.ufrgs.model;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

public class ConfigFileReader {
    public ConfigFileReader() {
    }
    public DiscardParameter loadDiscardParameter() {
        try {
            // The file path is obtained from the classpath, ensuring it works in both development and production environments.
            String filePath = Paths.get(
                    Objects.requireNonNull(
                            getClass().getClassLoader().getResource("config_alimentos.csv")).toURI())
                    .toString();
            DiscardParameter discardParameter = readParameterFromFile(filePath);
            return discardParameter;
        } catch (Exception e) {
            throw new RuntimeException("Error reading configuration file " +
        e.getMessage(), e);
        }
    }

    private static DiscardParameter readParameterFromFile(String filePath)
            throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        DiscardParameter discardParameter = new DiscardParameter();
        try {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].equals("margem_seguranca_dias")) {
                    if(fields[1].isEmpty()) {
                        throw new RuntimeException("Value for margem_seguranca_dias cannot be empty");
                    }else{
                        discardParameter.setMarginOfSafetyDays(Integer.parseInt(fields[1]));
                    }
                }else if (fields[0].equals("fator_descarte_percentual")) {
                    if(fields[1].isEmpty()) {
                        throw new RuntimeException("Value for fator_descarte_percentual cannot be empty");
                    }else{
                        discardParameter.setDiscardFactorPercentage(Double.parseDouble(fields[1]));
                    }
                } else {
                    throw new RuntimeException("Unknown parameter: " + fields[0]);
                }
            }
        } finally {
            br.close();
        }
        return discardParameter;
    }
}
