package service;

public class FixedRoyaltyStrategy implements IRoyaltyCalculationStrategy{
    private double fixedAmount;
    public FixedRoyaltyStrategy(double fixedAmount){
        this.fixedAmount = fixedAmount;
    }
    @Override
    public double calculateRoyalty(double branchRevenue) {
        return fixedAmount;
    }
}
