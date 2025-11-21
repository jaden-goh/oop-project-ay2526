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

/**
 * Main entry point and composition root of the Internship Placement Management System.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *     <li>Initialises managers, menus, and shared helpers</li>
 *     <li>Loads initial user data from CSV files</li>
 *     <li>Provides the top-level main menu (login, registration, quit)</li>
 *     <li>Routes authenticated users to role-specific menus</li>
 *     <li>Handles registration flows and CSV persistence</li>
 *     <li>Handles password reset and change flows</li>
 *     <li>Coordinates notification display and company rep approval persistence</li>
 * </ul>
 */

public class App {

    /** Validation pattern for student IDs (e.g., U1234567A). */
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^U\\d{7}[A-Z]$");

    /** Validation pattern for staff IDs (e.g., abc123). */
    private static final Pattern STAFF_ID_PATTERN = Pattern.compile("^[A-Za-z]{3}\\d{3}$");

    /** Generic email validation pattern. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** CSV header for student records. */
    private static final String STUDENT_HEADER = "StudentID,Name,Major,Year,Email";

    /** CSV header for staff records. */
    private static final String STAFF_HEADER = "StaffID,Name,Role,Department,Email";

    /** CSV header for company representative records. */
    private static final String COMPANY_HEADER = "CompanyRepID,Name,CompanyName,Department,Position,Email,Approved";

    /** Manages all user accounts and login/registration logic. */
    private final UserManager userManager = new UserManager();

    /** Manages internship postings and their lifecycle. */
    private final InternshipManager internshipManager = new InternshipManager();

    /** Handles applications and enforcement of application rules. */
    private final ApplicationManager applicationManager = new ApplicationManager();

    /** Dispatches notifications to users. */
    private final NotificationManager notificationManager = new NotificationManager();

    /** Manages withdrawal requests from students. */
    private final WithdrawalManager withdrawalManager = new WithdrawalManager();

    /** Generates reports for Career Center Staff. */
    private final ReportGenerator reportGenerator = new ReportGenerator();

    /** Shared scanner for console input. */
    private final Scanner scanner = new Scanner(System.in);

    /** Catalog of schools and majors used for selection. */
    private final SchoolMajorCatalog schoolMajorCatalog;

    /** Helper for console interaction. */
    private final ConsoleHelper console;

    /** Boundary class for browsing internships with filters. */
    private final InternshipBrowser internshipBrowser;

    /** Student-facing menu. */
    private final StudentMenu studentMenu;

    /** Company representative-facing menu. */
    private final CompanyRepMenu companyRepMenu;

    /** Career Center Staff-facing menu. */
    private final StaffMenu staffMenu;


    /** File path to student CSV data. */
    private final String studentDataPath;

    /** File path to staff CSV data. */
    private final String staffDataPath;

    /** File path to company representative CSV data. */
    private final String companyDataPath;

    /**
     * Constructs the application, initialising all managers, helpers, menus, and loading initial data.
     *
     * <p>Also wires callbacks for notification display, password change, and company rep approval
     * persistence into the role-specific menus.</p>
     */
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

    /**
     * Application entry point.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        new App().start();
    }

    /**
     * Starts the top-level main menu loop.
     *
     * <p>Options:</p>
     * <ul>
     *     <li>Login</li>
     *     <li>Register</li>
     *     <li>Quit</li>
     * </ul>
     */
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
                case "3" -> {
                    if (console.promptYesNo("Quit the application? (y/n): ", false)) {
                        running = false;
                    }
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
            System.out.println();
        }
        System.out.println("Goodbye.");
    }

    /**
     * Loads initial user accounts from CSV files for students, staff, and company representatives.
     */
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


    /**
     * Handles the login flow, including reset prompt and retry attempts.
     *
     * @return the authenticated {@link User}, or null if login is cancelled or fails
     */
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

    /**
     * Handles password reset for a given user ID.
     *
     * @param id user ID requesting a password reset
     */
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
        User target = userManager.findUserById(id);
        if (target != null && newPass.equals(target.getPassword())) {
            System.out.println("New password matches the old password. Please choose a different password.");
            return;
        }
        if (!userManager.resetPassword(id, newPass)) {
            System.out.println("Unable to reset password. Ensure the account exists and password meets requirements.");
        } else {
            System.out.println("Password updated. Use it to log in.");
        }
    }

    /**
     * Routes an authenticated user to their corresponding menu based on runtime type.
     *
     * @param user logged-in user
     */
    private void routeUser(User user) {
        switch (user) {
            case Student student -> studentMenu.show(student);
            case CompanyRep rep -> companyRepMenu.show(rep);
            case CareerCenterStaff staff -> staffMenu.show(staff);
            default -> {}
        }
    }

    /**
     * Handles the top-level registration flow for different user types.
     */
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

    /**
     * Handles registration of a new student user, including validation and CSV persistence.
     */
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

    /**
     * Handles registration of a new company representative, including CSV persistence and staff notification.
     */
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
        if (department == null || department.isBlank()) {
            department = "NA";
        }
        String position = console.readLine("Position: ");
        if (position == null || position.isBlank()) {
            position = "NA";
        }
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

    /**
     * Handles registration of a new Career Center Staff member, including CSV persistence.
     */
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

    /**
     * Persists a new student record in the student CSV file.
     */
    private void persistStudentRecord(String id, String name, String major, int year, String email) {
        String line = String.join(",", id, name, major, String.valueOf(year), email);
        appendCsvLine(studentDataPath, STUDENT_HEADER, line);
    }

    /**
     * Persists a new company representative record in the company CSV file.
     */
    private void persistCompanyRepRecord(String id, String name, String company, String department, String position) {
        String line = String.join(",", id, name, company, department, position, id, "false");
        appendCsvLine(companyDataPath, COMPANY_HEADER, line);
    }

    /**
     * Persists a new staff record in the staff CSV file.
     */
    private void persistStaffRecord(String id, String name, String department, String email) {
        String line = String.join(",", id, name, "Career Center Staff", department, email);
        appendCsvLine(staffDataPath, STAFF_HEADER, line);
    }

    /**
     * Appends a line to a CSV file, creating it with a header if it does not yet exist.
     *
     * @param path   file path
     * @param header header line to write if file does not exist
     * @param line   data line to append
     */
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

    /**
     * Checks whether a given email already exists in a specific CSV file.
     *
     * @param path             CSV file path
     * @param emailColumnIndex zero-based index of email column
     * @param targetEmail      email to search for
     * @return true if email already exists, false otherwise
     */
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

    /**
     * Handles in-session password change for a logged-in user.
     *
     * @param user the user who is changing their password
     */
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

    /**
     * Retrieves and displays notifications for the given user.
     *
     * @param user user whose notifications should be displayed
     */
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

    /**
     * Updates the approval status of a company representative in the backing CSV file.
     *
     * @param repId    representative ID (email)
     * @param approved new approval status
     */
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
