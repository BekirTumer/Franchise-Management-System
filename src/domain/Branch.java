package domain;

import java.util.ArrayList;
import java.util.List;

public class Branch {
    private String branchID;
    private String managerID;
    private boolean isActive;
    private String location;
    private String royaltyType; // "FIXED" veya "PERCENTAGE"
    private double royaltyValue;
    private double monthlyRevenue;
    private List<InventoryItem> inventory;

    public Branch(String branchID, String managerID,String location, boolean isActive, String royaltyType, double royaltyValue,  double monthlyRevenue) {
        this.branchID = branchID;
        this.managerID = managerID;
        this.isActive = isActive;
        this.location = location;
        this.royaltyType = royaltyType;
        this.royaltyValue = royaltyValue;
        this.monthlyRevenue = monthlyRevenue;
        this.inventory = new ArrayList<>();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBranchID() {
        return branchID;
    }

    public void setBranchID(String branchID) {
        this.branchID = branchID;
    }

    public String getManagerID() {
        return managerID;
    }

    public void setManagerID(String managerID) {
        this.managerID = managerID;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<InventoryItem> getInventory() {
        return inventory;
    }

    public void setInventory(List<InventoryItem> inventory) {
        this.inventory = inventory;
    }

    public String getRoyaltyType() {
        return royaltyType;
    }

    public void setRoyaltyType(String royaltyType) {
        this.royaltyType = royaltyType;
    }

    public double getRoyaltyValue() {
        return royaltyValue;
    }

    public void setRoyaltyValue(double royaltyValue) {
        this.royaltyValue = royaltyValue;
    }

    public double getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(double monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }
}
