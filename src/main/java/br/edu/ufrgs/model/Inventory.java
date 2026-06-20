package br.edu.ufrgs.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Inventory {
    //Sets stock status and predicted loss for a product
    public Product evaluateProduct(Product product, DiscardParameter config) {
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), product.getExpiryDate());

        if (daysUntilExpiry < 0) {
            product.setStockStatus(StockStatus.EXPIRED);
            product.setPredictedLoss(product.getPriceCost() * config.getDiscardFactorPercentage()); // Assuming factor percentage [0, 1]
        } else if (daysUntilExpiry <= config.getMarginOfSafetyDays()) {
            product.setStockStatus(StockStatus.ALERT);
            product.setPredictedLoss(0.0);
        } else {
            product.setStockStatus(StockStatus.OK);
            product.setPredictedLoss(0.0);
        }

        return product;
    }
    // Iterates through inventory while evaluating  
    public void evaluateInventory(List<Product> products, DiscardParameter config) {
        for (Product productIterate : products) {
            evaluateProduct(productIterate, config);
        }
    }
}
