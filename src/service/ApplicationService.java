package service;

import domain.ApplicationStatus;
import domain.FranchiseApplication;

import java.util.HashMap;
import java.util.Map;

public class ApplicationService {
    private Map<String, FranchiseApplication> applicatonDatabase= new HashMap<>();

    public void submitApplication(FranchiseApplication franchiseApplication){
        applicatonDatabase.put(franchiseApplication.getApplicationID(),  franchiseApplication);
    }
    public FranchiseApplication getApplication(String applicationID){
        return applicatonDatabase.get(applicationID);
    }
    public void updateStatus(String applicationID, ApplicationStatus newStatus){
        applicatonDatabase.get(applicationID).setStatus(newStatus);
    }
    public void WriteAllPendingApplications(){
        for( FranchiseApplication fa : applicatonDatabase.values()) {
            if (fa.getStatus() == ApplicationStatus.PENDING) {
                String tag = (fa.getAdminFeedback() != null) ? " [RESUBMITTED - EXTRA INFO ENTERED]" : " [NEW]";
                System.out.println("ID:" + fa.getApplicationID() + " Tag:" + tag);
            }
        }
    }
}
