package boundary;

import control.ApplicationManager;
import control.InternshipManager;
import control.NotificationManager;
import control.ReportGenerator;
import control.SchoolMajorCatalog;
import control.UserManager;
import control.WithdrawalManager;
import entity.CareerCenterStaff;
import entity.CompanyRep;
import entity.Notification;
import entity.Student;
import entity.User;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import menu.CompanyRepMenu;
import menu.StaffMenu;
import menu.StudentMenu;

public class App {
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^U\\d{7}[A-Z]$");
    private static final Pattern STAFF_ID_PATTERN = Pattern.compile("^[A-Za-z]{3}\\d{3}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String STUDENT_HEADER = "StudentID,Name,Major,Year,Email";
    private static final String STAFF_HEADER = "StaffID,Name,Role,Department,Email";
    private static final String COMPANY_HEADER = "CompanyRepID,Name,CompanyName,Department,Position,Email,Approved";

    private final UserManager userManager = new UserManager();
    private final InternshipManager internshipManager = new InternshipManager();
    private final ApplicationManager applicationManager = new ApplicationManager();
    private final NotificationManager notificationManager = new NotificationManager();
    private final WithdrawalManager withdrawalManager = new WithdrawalManager();
    private final ReportGenerator reportGenerator = new ReportGenerator();
    private final Scanner scanner = new Scanner(System.in);
    private final SchoolMajorCatalog schoolMajorCatalog;
    private final ConsoleHelper console;
    private final InternshipBrowser internshipBrowser;
    private final StudentMenu studentMenu;
    private final CompanyRepMenu companyRepMenu;
    private final StaffMenu staffMenu;
    private final String studentDataPath;
    private final String staffDataPath;
    private final String companyDataPath;

    public App() {
        this.studentDataPath = "data/sample_student_list.csv";
        this.staffDataPath = "data/sample_staff_list.csv";
        this.companyDataPath = "data/sample_company_representative_list.csv";
        this.schoolMajorCatalog = new SchoolMajorCatalog(new File("data/schools_and_majors.csv"));
        this.console = new ConsoleHelper(scanner, schoolMajorCatalog);
        this.internshipBrowser = new InternshipBrowser(internshipManager, console);
        this.studentMenu = new StudentMenu(console, internshipBrowser, applicationManager,
                internshipManager, notificationManager, withdrawalManager, userManager,
                this::displayNotifications, this::handlePasswordChange);
        this.companyRepMenu = new CompanyRepMenu(console, internshipBrowser, internshipManager,
                applicationManager, notificationManager, userManager,
                this::displayNotifications, this::handlePasswordChange);
        this.staffMenu = new StaffMenu(console, internshipManager, userManager, withdrawalManager,
                notificationManager, reportGenerator, this::displayNotifications,
                this::handlePasswordChange, this::updateCompanyRepApproval);
        loadInitialUsers();
        applicationManager.setNotificationManager(notificationManager);
    }

    public static void main(String[] args) {
        new App().start();
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("=== Internship Hub ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Quit");
            String choice = console.readLine("Select an option: ");
            switch (choice) {
                case "1" -> {
                    User user = promptLogin();
                    if (user != null) {
                        routeUser(user);
                    }
                }
                case "2" -> handleRegistration();
                case "3" -> running = false;
                default -> System.out.println("Invalid choice. Please try again.");
            }
            System.out.println();
        }
        System.out.println("Goodbye.");
    }

    private void loadInitialUsers() {
        File studentCsv = new File(studentDataPath);
        File staffCsv = new File(staffDataPath);
        File companyCsv = new File(companyDataPath);
        try {
            userManager.loadAllUsers(studentCsv, staffCsv, companyCsv);
        } catch (IllegalStateException e) {
            System.err.println("Failed to load user data: " + e.getMessage());
        }
    }


    private User promptLogin() {
        int attempts = 0;
        while (true) {
            String id = console.readLine("User ID (or 'cancel' to return): ");
            if (id.equalsIgnoreCase("cancel")) {
                System.out.println("Login cancelled.");
                return null;
            }
            String password = console.readLine("Password (type 'reset' to reset, 'cancel' to exit): ");
            if ("cancel".equalsIgnoreCase(password)) {
                System.out.println("Login cancelled.");
                return null;
            }
            if ("reset".equalsIgnoreCase(password)) {
                handlePasswordReset(id);
                continue;
            }
            User user = userManager.login(id, password);
            if (user != null) {
                System.out.println("Welcome back, " + user.getName() + ".");
                return user;
            }
            System.out.println(userManager.getLastLoginMessage());
            attempts++;
            if (!console.promptYesNo("Try again? (y/n): ", true)) {
                return null;
            }
            if (attempts >= 3) {
                System.out.println("Tip: consider using the reset option if you continue to face issues.");
            }
        }
    }

    private void handlePasswordReset(String id) {
        if (id == null || id.isBlank()) {
            System.out.println("Provide your user ID before requesting a reset.");
            return;
        }
        String newPass = console.readLine("Enter new temporary password (min 8 chars) or 'cancel': ");
        if ("cancel".equalsIgnoreCase(newPass)) {
            System.out.println("Reset cancelled.");
            return;
        }
        String confirm = console.readLine("Confirm new temporary password: ");
        if (!newPass.equals(confirm)) {
            System.out.println("Passwords do not match. Reset cancelled.");
            return;
        }
        if (!userManager.resetPassword(id, newPass)) {
            System.out.println("Unable to reset password. Ensure the account exists and password meets requirements.");
        } else {
            System.out.println("Password updated. Use it to log in.");
        }
    }

    private void routeUser(User user) {
        switch (user) {
            case Student student -> studentMenu.show(student);
            case CompanyRep rep -> companyRepMenu.show(rep);
            case CareerCenterStaff staff -> staffMenu.show(staff);
            default -> {}
        }
    }

    private void handleRegistration() {
        System.out.println("Select user type to register:");
        System.out.println("1. Student");
        System.out.println("2. Company Representative");
        System.out.println("3. Career Center Staff");
        System.out.println("4. Cancel");
        String choice = console.readLine("Choice: ");
        switch (choice) {
            case "1" -> registerStudent();
            case "2" -> registerCompanyRep();
            case "3" -> registerCareerCenterStaff();
            case "4" -> System.out.println("Registration cancelled.");
            default -> System.out.println("Unknown user type.");
        }
    }

    private void registerStudent() {
        String id = console.readLine("Student ID (e.g., U1234567A): ").toUpperCase();
        if (!STUDENT_ID_PATTERN.matcher(id).matches()) {
            System.out.println("Invalid ID format.");
            return;
        }
        String name = console.readLine("Name: ");
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        String password = console.promptPasswordInput("Password (min 8 chars): ");
        if (password == null) {
            System.out.println("Registration cancelled.");
            return;
        }
        String email = console.readLine("Email (must end with @e.ntu.edu.sg): ");
        String normalizedEmail = email.toLowerCase();
        if (email.isEmpty()
                || !EMAIL_PATTERN.matcher(email).matches()
                || !normalizedEmail.endsWith("@e.ntu.edu.sg")) {
            System.out.println("A valid NTU email is required.");
            return;
        }
        if (emailExistsInCsv(studentDataPath, 4, email)) {
            System.out.println("This email is already registered.");
            return;
        }
        Integer year = console.readInt("Year of Study (1-4, or type 'cancel'): ", 1, 4, null, true);
        if (year == null) {
            System.out.println("Registration cancelled.");
            return;
        }
        String major = console.promptStudentMajorSelection();
        if (!console.promptYesNo("Confirm registration? (y/n): ", true)) {
            System.out.println("Registration cancelled.");
            return;
        }
        boolean registered = userManager.registerStudent(id, name, password, year, major);
        if (registered) {
            persistStudentRecord(id, name, major, year, email);
            System.out.println("Student registered successfully.");
        } else {
            System.out.println("Registration failed. Ensure ID is unique and password meets requirements.");
        }
    }

    private void registerCompanyRep() {
        String id = console.readLine("Company Rep ID (email): ");
        if (!EMAIL_PATTERN.matcher(id).matches()) {
            System.out.println("Invalid email format.");
            return;
        }
        String name = console.readLine("Name: ");
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        String password = console.promptPasswordInput("Password (min 8 chars): ");
        if (password == null) {
            System.out.println("Registration cancelled.");
            return;
        }
        if (emailExistsInCsv(companyDataPath, 5, id)) {
            System.out.println("This email is already registered.");
            return;
        }
        String companyName = console.readLine("Company Name: ");
        if (companyName.isEmpty()) {
            System.out.println("Company name is required.");
            return;
        }
        String department = console.readLine("Department: ");
        String position = console.readLine("Position: ");
        if (!console.promptYesNo("Submit registration for approval? (y/n): ", true)) {
            System.out.println("Registration cancelled.");
            return;
        }
        boolean registered = userManager.registerCompanyRep(id, name, password, companyName, department, position, false);
        if (registered) {
            persistCompanyRepRecord(id, name, companyName, department, position);
            System.out.println("Registration submitted. A Career Center Staff member must approve your account before you can log in.");
            notificationManager.notifyStaffNewRepRegistration(
                    userManager.getCareerCenterStaffMembers(), name, id, companyName);
        } else {
            System.out.println("Registration failed. Ensure all fields are valid and the ID has not been used.");
        }
    }

    private void registerCareerCenterStaff() {
        String id = console.readLine("Staff ID (e.g., abc123): ");
        if (!STAFF_ID_PATTERN.matcher(id).matches()) {
            System.out.println("Invalid staff ID format.");
            return;
        }
        String name = console.readLine("Name: ");
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        String password = console.promptPasswordInput("Password (min 8 chars): ");
        if (password == null) {
            System.out.println("Registration cancelled.");
            return;
        }
        String department = console.readLine("Department: ");
        String email = console.readLine("Email (must end with @ntu.edu.sg): ");
        String normalizedEmail = email.toLowerCase();
        if (email.isEmpty()
                || !EMAIL_PATTERN.matcher(email).matches()
                || !normalizedEmail.endsWith("@ntu.edu.sg")) {
            System.out.println("A valid NTU email is required.");
            return;
        }
        if (emailExistsInCsv(staffDataPath, 4, email)) {
            System.out.println("This email is already registered.");
            return;
        }
        if (!console.promptYesNo("Confirm staff registration? (y/n): ", true)) {
            System.out.println("Registration cancelled.");
            return;
        }
        boolean registered = userManager.registerCareerCenterStaff(id, name, password, department);
        if (registered) {
            persistStaffRecord(id, name, department, email);
            System.out.println("Career Center Staff registered successfully.");
        } else {
            System.out.println("Registration failed. Ensure the ID is unique and password meets requirements.");
        }
    }

    private void persistStudentRecord(String id, String name, String major, int year, String email) {
        String line = String.join(",", id, name, major, String.valueOf(year), email);
        appendCsvLine(studentDataPath, STUDENT_HEADER, line);
    }

    private void persistCompanyRepRecord(String id, String name, String company, String department, String position) {
        String line = String.join(",", id, name, company, department, position, id, "false");
        appendCsvLine(companyDataPath, COMPANY_HEADER, line);
    }

    private void persistStaffRecord(String id, String name, String department, String email) {
        String line = String.join(",", id, name, "Career Center Staff", department, email);
        appendCsvLine(staffDataPath, STAFF_HEADER, line);
    }

    private void appendCsvLine(String path, String header, String line) {
        File file = new File(path);
        boolean exists = file.exists();
        try (FileWriter writer = new FileWriter(file, true)) {
            if (!exists) {
                writer.write(header);
                writer.write(System.lineSeparator());
            }
            writer.write(line);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Failed to persist record to " + path + ": " + e.getMessage());
        }
    }

    private boolean emailExistsInCsv(String path, int emailColumnIndex, String targetEmail) {
        if (targetEmail == null || targetEmail.isBlank()) {
            return false;
        }
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        String normalizedTarget = targetEmail.trim().toLowerCase();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                String[] tokens = line.split(",", -1);
                if (tokens.length > emailColumnIndex) {
                    String existing = tokens[emailColumnIndex].trim().toLowerCase();
                    if (!existing.isEmpty() && existing.equals(normalizedTarget)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read " + path + " while checking duplicate emails: " + e.getMessage());
        }
        return false;
    }

    private void handlePasswordChange(User user) {
        while (true) {
            String newPassword = console.readLine("New password (min 8 chars, type 'cancel' to exit): ");
            if ("cancel".equalsIgnoreCase(newPassword)) {
                System.out.println("Password change cancelled.");
                return;
            }
            String confirm = console.readLine("Confirm new password: ");
            if (!newPassword.equals(confirm)) {
                System.out.println("Passwords do not match. Try again.");
                continue;
            }
            try {
                user.changePassword(newPassword);
                System.out.println("Password updated successfully.");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to change password: " + e.getMessage());
            }
        }
    }

    private void displayNotifications(User user) {
        if (user == null) {
            return;
        }
        List<Notification> notifications = notificationManager.consumeNotifications(user);
        if (notifications.isEmpty()) {
            return;
        }
        System.out.println("\n--- Notifications ---");
        for (Notification notification : notifications) {
            System.out.println("* " + notification);
        }
        System.out.println("---------------------");
    }

    private void updateCompanyRepApproval(String repId, boolean approved) {
        File file = new File(companyDataPath);
        if (!file.exists() || repId == null || repId.isBlank()) {
            return;
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Failed to read representative file: " + e.getMessage());
            return;
        }

        if (lines.size() <= 1) {
            return;
        }
        boolean updated = false;
        for (int i = 1; i < lines.size(); i++) {
            String[] tokens = lines.get(i).split(",", -1);
            if (tokens.length < 7) {
                continue;
            }
            if (tokens[0].trim().equalsIgnoreCase(repId.trim())) {
                tokens[6] = String.valueOf(approved);
                lines.set(i, String.join(",", tokens));
                updated = true;
                break;
            }
        }
        if (!updated) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (String entry : lines) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to update representative file: " + e.getMessage());
        }
    }
}
