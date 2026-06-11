package br.edu.ufrgs.model;

public class DiscardParameter {
    private int marginOfSafetyDays;
    private double discardFactorPercentage;

    public DiscardParameter() {}

    public DiscardParameter(int marginOfSafetyDays, double discardFactorPercentage) {
        this.marginOfSafetyDays = marginOfSafetyDays;
        setDiscardFactorPercentage(discardFactorPercentage);
    }

    public int getMarginOfSafetyDays() {
        return marginOfSafetyDays;
    }

    public void setMarginOfSafetyDays(int marginOfSafetyDays) {
        this.marginOfSafetyDays = marginOfSafetyDays;
    }

    public double getDiscardFactorPercentage() {
        return discardFactorPercentage;
    }

    public void setDiscardFactorPercentage(double discardFactorPercentage) {
        if (discardFactorPercentage < 0 || discardFactorPercentage > 1) {
            throw new IllegalArgumentException("discardFactorPercentage must be between 0 and 1");
        }
        this.discardFactorPercentage = discardFactorPercentage;
    }

    public String toString() {
        return "DiscardParameter{" +
                "marginOfSafetyDays=" + marginOfSafetyDays +
                ", discardFactorPercentage=" + discardFactorPercentage +
                '}';
    }
}