package service;

import domain.ApplicationStatus;

public class FranchiseFacade {
    private ApplicationService applicationService;
    private BranchManagementService branchManagementService;
    public FranchiseFacade(ApplicationService applicationService, BranchManagementService branchManagementService) {
        this.applicationService = applicationService;
        this.branchManagementService = branchManagementService;
    }
    public void approveAndOpenBranch(String appID, String branchId, String managerID, String location, String royaltyType, double  royaltyValue) {
        applicationService.updateStatus(appID, ApplicationStatus.APPROVED);
        branchManagementService.createBranch(branchId,managerID,location,royaltyType,royaltyValue);
        System.out.println("The application has been approved and a new branch has opened in ! " + location);
    }
}
