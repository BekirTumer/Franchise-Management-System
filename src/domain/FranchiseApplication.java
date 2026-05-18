package domain;

import java.util.ArrayList;
import java.util.List;

public class FranchiseApplication {
    private String applicationID;
    private String applicantID;
    private ApplicationStatus status;
    private String financialData;
    private String personalData;
    private String adminFeedback;
    private String location;
    private List<Document> documents;



    public FranchiseApplication(String applicationID, String applicantID, ApplicationStatus status, String financialData, String personalData, String location) {
        this.applicationID = applicationID;
        this.applicantID = applicantID;
        this.status = status;
        this.financialData = financialData;
        this.personalData = personalData;
        this.location = location;
        documents = new ArrayList<>();
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getApplicantID() {
        return applicantID;
    }

    public void setApplicantID(String applicantID) {
        this.applicantID = applicantID;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getFinancialData() {
        return financialData;
    }

    public void setFinancialData(String financialData) {
        this.financialData = financialData;
    }

    public String getPersonalData() {
        return personalData;
    }

    public void setPersonalData(String personalData) {
        this.personalData = personalData;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
    public String getAdminFeedback() {
        return adminFeedback;
    }

    public void setAdminFeedback(String adminFeedback) {
        this.adminFeedback = adminFeedback;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
