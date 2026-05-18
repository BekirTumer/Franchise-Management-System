package domain;

import java.util.List;

public class RestockRequest {
    private String requestId;
    private String branchId;
    private List<String> requestedItems;
    private String requestedDate;
    private RestockStatus status;

    public RestockRequest(String requestId, String branchId, List requestedItems, RestockStatus status, String requestedDate) {
        this.requestId = requestId;
        this.branchId = branchId;
        this.requestedItems = requestedItems;
        this.status = status;
        this.requestedDate = requestedDate;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public List getRequestedItems() {
        return requestedItems;
    }

    public void setRequestedItems(List requestedItems) {
        this.requestedItems = requestedItems;
    }

    public String getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(String requestedDate) {
        this.requestedDate = requestedDate;
    }

    public RestockStatus getStatus() {
        return status;
    }

    public void setStatus(RestockStatus status) {
        this.status = status;
    }
}
