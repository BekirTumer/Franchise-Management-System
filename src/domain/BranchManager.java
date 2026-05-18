package domain;

public class BranchManager extends User {
    private String managedBranchID;
    public BranchManager(String userID, String username, String password, UserRole role, String managedBranchID) {
        super(userID,  username,  password,  role);
        this.managedBranchID = managedBranchID;
    }

    public String getManagedBranchID() {
        return managedBranchID;
    }

    public void setManagedBranchID(String managedBranchID) {
        this.managedBranchID = managedBranchID;
    }
}
