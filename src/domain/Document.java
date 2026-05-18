package domain;

public class Document {
    private String documentID;
    private String applicationID;
    private String documentType;
    private String content;

    public Document(String documentID, String applicationID, String documentType, String content) {
        this.documentID = documentID;
        this.applicationID = applicationID;
        this.documentType = documentType;
        this.content = content;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
