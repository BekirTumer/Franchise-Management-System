package service;

import domain.*;

public class UserFactory {
    public static User createUser(UserRole role, String id, String username, String password) {
        switch (role) {
            case APPLICANT:
                return new Applicant(id, username, password,role);
            case HQ_ADMIN:
                return new HQAdmin(id, username, password,role);
            case BRANCH_MANAGER:
                return new BranchManager(id, username, password,role,"");
        }
        return null;
    }
}
