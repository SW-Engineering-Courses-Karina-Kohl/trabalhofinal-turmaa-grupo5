package br.edu.ufrgs.model;

public class DiscardParameter {
    private int marginOfSafetyDays;
    private double discardFactorPercentage;

    public DiscardParameter() {}

    public DiscardParameter(int marginOfSafetyDays, double discardFactorPercentage) {
        this.marginOfSafetyDays = marginOfSafetyDays;
        this.discardFactorPercentage = discardFactorPercentage;
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
        this.discardFactorPercentage = discardFactorPercentage;
    }

    public String toString() {
        return "DiscardParameter{" +
                "marginOfSafetyDays=" + marginOfSafetyDays +
                ", discardFactorPercentage=" + discardFactorPercentage +
                '}';
    }
}