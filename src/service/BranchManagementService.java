package service;

import domain.Branch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BranchManagementService {
    private Map<String, Branch> branchDatabase = new HashMap<>();

    public Branch createBranch(String branchId, String managerId, String location,String royaltyType, double royaltyValue) {
        double revenue = Math.random() * 50000 + 50000;
        Branch newBranch = new Branch(branchId, managerId, location, true, royaltyType, royaltyValue,revenue);
        branchDatabase.put(branchId, newBranch);
        return newBranch;
    }
    public Branch getBranch(String branchId){return branchDatabase.get(branchId);}
    public Collection<Branch> getAllBranches() {
        return branchDatabase.values();
    }
}
