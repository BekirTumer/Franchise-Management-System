package service;

import domain.RestockRequest;
import domain.RestockStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class InventoryManagementService {
    private Map<String, RestockRequest> restockDatabase = new HashMap<>();

    public void addRestockRequest(RestockRequest request) {
        restockDatabase.put(request.getRequestId(), request);
    }

    public RestockRequest getRestockRequest(String requestId) {
        return restockDatabase.get(requestId);
    }
    public Collection<RestockRequest> getAllRequests() {
        return restockDatabase.values();
    }
    public void updateRestockStatus(String requestId, RestockStatus newStatus) {
        RestockRequest req = restockDatabase.get(requestId);
        if (req != null) {
            req.setStatus(newStatus);
        }
    }
}
