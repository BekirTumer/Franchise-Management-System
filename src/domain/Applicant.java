package domain;

public class Applicant extends User {
    public Applicant(String userID, String username, String password, UserRole role){
        super( userID,  username,  password,  role);
    }
}
