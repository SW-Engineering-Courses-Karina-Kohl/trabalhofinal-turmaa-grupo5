package br.edu.ufrgs.model;

import java.util.List;

public class InventoryFileWriter {

    private static final String CSV_HEADER =
            "id,produto,categoria,data_validade,preco_custo,status_estoque,prejuizo_estimado";

    public String export(List<Product> produtos) {
        StringBuilder builder = new StringBuilder();
        builder.append(CSV_HEADER);
        return builder.toString();
    }
}
