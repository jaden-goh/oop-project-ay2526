package modular.service;

import modular.model.ModCareerCenterStaff;
import modular.model.ModCompanyRep;
import modular.model.ModStudent;
import modular.model.ModUser;

import java.util.Collection;
import java.util.regex.Pattern;

public class ModAuthenticationService {
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^U\\d{7}[A-Za-z]$");
    private final Collection<ModUser> users;

    public ModAuthenticationService(Collection<ModUser> users) {
        this.users = users;
    }

    public ModStudent authenticateStudent(String id, String password) {
        if (!STUDENT_ID_PATTERN.matcher(id).matches()) {
            System.out.println("Invalid student ID.");
            return null;
        }
        ModUser user = findUserById(id, ModStudent.class);
        if (user == null) {
            System.out.println("Student not found.");
            return null;
        }
        if (!user.verifyPassword(password)) {
            System.out.println("Incorrect password.");
            return null;
        }
        return (ModStudent) user;
    }

    public ModCompanyRep authenticateCompanyRep(String id, String password) {
        ModUser user = findUserById(id, ModCompanyRep.class);
        if (user == null) {
            System.out.println("Company Rep not found.");
            return null;
        }
        if (!user.verifyPassword(password)) {
            System.out.println("Incorrect password.");
            return null;
        }
        ModCompanyRep rep = (ModCompanyRep) user;
        if (!rep.isAuthorised()) {
            System.out.println("Account pending approval.");
            return null;
        }
        return rep;
    }

    public ModCareerCenterStaff authenticateStaff(String id, String password) {
        ModUser user = findUserById(id, ModCareerCenterStaff.class);
        if (user == null) {
            System.out.println("Staff not found.");
            return null;
        }
        if (!user.verifyPassword(password)) {
            System.out.println("Incorrect password.");
            return null;
        }
        return (ModCareerCenterStaff) user;
    }

    private ModUser findUserById(String id, Class<? extends ModUser> type) {
        if (users == null) return null;
        for (ModUser user : users) {
            if (type.isInstance(user) && user.getId().equalsIgnoreCase(id)) {
                return user;
            }
        }
        return null;
    }
}
