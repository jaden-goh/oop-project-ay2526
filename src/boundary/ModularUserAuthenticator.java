package boundary;

import modular.model.ModCareerCenterStaff;
import modular.model.ModCompanyRep;
import modular.model.ModStudent;
import modular.service.ModAuthenticationService;
import modular.service.ModRegistrationService;
import modular.service.dto.ModCompanyRepRegistrationData;
import modular.service.dto.ModStaffRegistrationData;
import modular.service.dto.ModStudentRegistrationData;

import java.util.Scanner;

/**
 * Alternative authenticator that delegates validation and persistence
 * to the new AuthenticationService and RegistrationService.
 */
public class ModularUserAuthenticator {
    private final Scanner scanner;
    private final ModAuthenticationService authenticationService;
    private final ModRegistrationService registrationService;

    public ModularUserAuthenticator(Scanner scanner,
                                    ModAuthenticationService authenticationService,
                                    ModRegistrationService registrationService) {
        this.scanner = scanner;
        this.authenticationService = authenticationService;
        this.registrationService = registrationService;
    }

    public ModStudent handleStudentLogin() {
        System.out.print("Enter Student ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        return authenticationService.authenticateStudent(id, password);
    }

    public ModCompanyRep handleCompanyRepLogin() {
        System.out.print("Enter Company Rep ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();
        return authenticationService.authenticateCompanyRep(id, password);
    }

    public ModCareerCenterStaff handleCareerStaffLogin() {
        System.out.print("Enter Career Staff ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        return authenticationService.authenticateStaff(id, password);
    }

    public void register() {
        System.out.println("I am a:\n1. Student\n2. Company Representative\n3. Career Center Staff\n4. Return");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> registerStudent();
            case "2" -> registerCompanyRep();
            case "3" -> registerStaff();
            default -> System.out.println("Returning to main menu.");
        }
    }

    private void registerStudent() {
        System.out.println("=== Student Registration ===");
        System.out.print("Enter Student ID: ");
        String studentID = scanner.nextLine().trim();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Major: ");
        String major = scanner.nextLine().trim();
        System.out.print("Enter Year of Study: ");
        String year = scanner.nextLine().trim();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();

        if (!confirmDetails(new String[][]{
                {"Student ID", studentID},
                {"Name", name},
                {"Major", major},
                {"Year of Study", year},
                {"Email", email}
        })) {
            System.out.println("Registration cancelled. Please start over.");
            return;
        }

        registrationService.registerStudent(
                new ModStudentRegistrationData(studentID, name, major, year, email)
        );
    }

    private void registerCompanyRep() {
        System.out.println("=== Company Representative Registration ===");
        System.out.print("Enter Company Email: ");
        String email = scanner.nextLine().trim();
        String[] parts = extractEmailParts(email);
        if (parts == null) {
            System.out.println("Invalid company email format.");
            return;
        }
        String companyRepId = parts[0];
        String companyName = parts[1];
        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Department: ");
        String department = scanner.nextLine().trim();
        System.out.print("Enter Position: ");
        String position = scanner.nextLine().trim();

        if (!confirmDetails(new String[][]{
                {"Company Rep ID", companyRepId},
                {"Name", name},
                {"Company Name", companyName},
                {"Department", department},
                {"Position", position},
                {"Email", email}
        })) {
            System.out.println("Registration cancelled. Please start over.");
            return;
        }

        registrationService.registerRep(
                new ModCompanyRepRegistrationData(
                        companyRepId,
                        name,
                        companyName,
                        department,
                        position,
                        email
                )
        );
        System.out.println("Registration submitted! Your Company Rep ID is: " + companyRepId +
                ". Please wait for approval from Career Center Staff before logging in.");
    }

    private void registerStaff() {
        System.out.println("=== Career Staff Registration ===");
        System.out.print("Enter Staff ID: ");
        String staffId = scanner.nextLine().trim();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Role: ");
        String role = scanner.nextLine().trim();
        System.out.print("Enter Department: ");
        String department = scanner.nextLine().trim();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();

        if (!confirmDetails(new String[][]{
                {"Staff ID", staffId},
                {"Name", name},
                {"Role", role},
                {"Department", department},
                {"Email", email}
        })) {
            System.out.println("Registration cancelled. Please start over.");
            return;
        }

        registrationService.registerStaff(
                new ModStaffRegistrationData(staffId, name, role, department, email)
        );
    }

    private boolean confirmDetails(String[][] labelsAndValues) {
        System.out.println("Confirm your details:");
        for (String[] pair : labelsAndValues) {
            System.out.println(pair[0] + ": " + pair[1]);
        }
        System.out.print("Is the information correct? (Y/N): ");
        String confirm = scanner.nextLine().trim();
        return confirm.equalsIgnoreCase("Y");
    }

    public static String[] extractEmailParts(String email) {
        if (email == null) return null;
        int atIndex = email.indexOf("@");
        int dotIndex = email.lastIndexOf(".");
        if (atIndex == -1 || dotIndex == -1 || dotIndex < atIndex) {
            return null;
        }
        String user = email.substring(0, atIndex);
        String company = email.substring(atIndex + 1, dotIndex);
        return new String[]{user, company};
    }
}
