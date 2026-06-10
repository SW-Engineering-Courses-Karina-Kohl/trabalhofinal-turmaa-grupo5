package br.edu.ufrgs.model;
import java.time.LocalDate;

public class Product {
    
    private int id;
    private String prodName;
    private String category;
    private LocalDate expiryDate;
    private double priceCost;
    private String stockStatus;
    private double predictedLoss;

    public Product(int id, String prodName, String category, LocalDate expiryDate, double priceCost){
        this.id = id;
        this.prodName = prodName;
        this.category = category;
        this.expiryDate = expiryDate;
        this.priceCost = priceCost;
    }

    public int getId(){
        return id;
    }

    public String getProdName(){
        return prodName;
    }

    public String getCategory(){
        return category;
    }

    public LocalDate getExpiryDate(){
        return expiryDate;
    }

    public double getPredictedLoss() {
        return predictedLoss;
    }

    public void setPredictedLoss(double predictedLoss) {
        if(predictedLoss < 0){
            throw new IllegalArgumentException("predictedLoss cannot be negative");
        }
        
        this.predictedLoss = predictedLoss;
    }

    public double getPriceCost() {
        return priceCost;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public String debugGetMessage(){
        return "Product: " + "ID: " + this.getId() + " Name: " + this.getProdName() + " Category: " + this.getCategory() + " Cost: " 
        + this.getPriceCost() + " ExpDate: " + this.getExpiryDate() + " Loss: " + this.getPredictedLoss() + " Status: " + this.getStockStatus(); 
    }
}
