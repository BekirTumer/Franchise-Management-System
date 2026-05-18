package domain;

public class InventoryItem {
    private String itemID;
    private String itemName;
    private String branchID;
    private int currentQuantity;
    private int thresholdQuantity;

    public InventoryItem(String itemID, String itemName, String branchID, int currentQuantity, int thresholdQuantity) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.branchID = branchID;
        this.currentQuantity = currentQuantity;
        this.thresholdQuantity = thresholdQuantity;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getBranchID() {
        return branchID;
    }

    public void setBranchID(String branchID) {
        this.branchID = branchID;
    }

    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(int currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public int getThresholdQuantity() {
        return thresholdQuantity;
    }

    public void setThresholdQuantity(int thresholdQuantity) {
        this.thresholdQuantity = thresholdQuantity;
    }
}
