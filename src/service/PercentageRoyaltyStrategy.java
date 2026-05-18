package service;

public class PercentageRoyaltyStrategy implements IRoyaltyCalculationStrategy {
 private double percentageRate;
 public PercentageRoyaltyStrategy(double percentageRate) {
     this.percentageRate = percentageRate/100;
 }
 @Override
    public double calculateRoyalty(double branchRevenue) {
     return branchRevenue*percentageRate;
 }
}
