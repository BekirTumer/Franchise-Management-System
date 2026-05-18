package domain;

public class RoyaltyPayment {
    private String paymentId;
    private String branchId;
    private double amount;
    private String paymentDate;
    private boolean isSuccessful;

    public RoyaltyPayment(String paymentId, String branchId, double amount, String paymentDate, boolean successful) {
        this.paymentId = paymentId;
        this.branchId = branchId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.isSuccessful = successful;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }
}
