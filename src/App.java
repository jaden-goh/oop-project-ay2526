import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

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
    private final WithdrawalManager withdrawalManager = new WithdrawalManager();
    private final ReportGenerator reportGenerator = new ReportGenerator();
    private final Scanner scanner = new Scanner(System.in);
    private final String studentDataPath;
    private final String staffDataPath;
    private final String companyDataPath;

    public App() {
        this.studentDataPath = "data/sample_student_list.csv";
        this.staffDataPath = "data/sample_staff_list.csv";
        this.companyDataPath = "data/sample_company_representative_list.csv";
        loadInitialUsers();
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
            System.out.print("Select an option: ");
            String choice = scanner.nextLine().trim();
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
            System.out.print("User ID (or 'cancel' to return): ");
            String id = scanner.nextLine().trim();
            if (id.equalsIgnoreCase("cancel")) {
                System.out.println("Login cancelled.");
                return null;
            }
            String password = readPassword("Password (type 'reset' to reset, 'cancel' to exit)");
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
            if (!promptYesNo("Try again? (y/n): ", true)) {
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
        System.out.print("Enter new temporary password (min 8 chars) or 'cancel': ");
        String newPass = scanner.nextLine();
        if ("cancel".equalsIgnoreCase(newPass)) {
            System.out.println("Reset cancelled.");
            return;
        }
        if (!userManager.resetPassword(id, newPass)) {
            System.out.println("Unable to reset password. Ensure the account exists and password meets requirements.");
        } else {
            System.out.println("Password updated. Use it to log in.");
        }
    }

    private String readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword(prompt + ": ");
            return chars == null ? "" : new String(chars);
        }
        System.out.print(prompt + " (input visible): ");
        return scanner.nextLine();
    }

    private void routeUser(User user) {
        switch (user) {
            case Student student -> showStudentMenu(student);
            case CompanyRep rep -> showRepMenu(rep);
            case CareerCenterStaff staff -> showStaffMenu(staff);
            default -> {}
        }
    }

    private void handleRegistration() {
        System.out.println("Select user type to register:");
        System.out.println("1. Student");
        System.out.println("2. Company Representative");
        System.out.println("3. Career Center Staff");
        System.out.println("4. Cancel");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> registerStudent();
            case "2" -> registerCompanyRep();
            case "3" -> registerCareerCenterStaff();
            case "4" -> System.out.println("Registration cancelled.");
            default -> System.out.println("Unknown user type.");
        }
    }

    private void registerStudent() {
        System.out.print("Student ID (e.g., U1234567A): ");
        String id = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
        if (!STUDENT_ID_PATTERN.matcher(id).matches()) {
            System.out.println("Invalid ID format.");
            return;
        }
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        String password = promptPasswordInput("Password (min 8 chars): ");
        if (password == null) {
            return;
        }
        Integer year = readInt("Year of Study (1-4, or type 'cancel'): ", 1, 4, null, true);
        if (year == null) {
            System.out.println("Registration cancelled.");
            return;
        }
        System.out.print("Major: ");
        String major = scanner.nextLine().trim();
        if (!promptYesNo("Confirm registration? (y/n): ", true)) {
            System.out.println("Registration cancelled.");
            return;
        }
        boolean registered = userManager.registerStudent(id, name, password, year, major);
        if (registered) {
            persistStudentRecord(id, name, major, year);
            System.out.println("Student registered successfully.");
        } else {
            System.out.println("Registration failed. Ensure ID is unique and password meets requirements.");
        }
    }

    private void registerCompanyRep() {
        System.out.print("Company Rep ID (email): ");
        String id = scanner.nextLine().trim();
        if (!EMAIL_PATTERN.matcher(id).matches()) {
            System.out.println("Invalid email format.");
            return;
        }
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        String password = promptPasswordInput("Password (min 8 chars): ");
        if (password == null) {
            return;
        }
        System.out.print("Company Name: ");
        String companyName = scanner.nextLine().trim();
        if (companyName.isEmpty()) {
            System.out.println("Company name is required.");
            return;
        }
        System.out.print("Department: ");
        String department = scanner.nextLine().trim();
        System.out.print("Position: ");
        String position = scanner.nextLine().trim();
        if (!promptYesNo("Submit registration for approval? (y/n): ", true)) {
            System.out.println("Registration cancelled.");
            return;
        }
        boolean registered = userManager.registerCompanyRep(id, name, password, companyName, department, position, false);
        if (registered) {
            persistCompanyRepRecord(id, name, companyName, department, position);
            System.out.println("Registration submitted. A Career Center Staff member must approve your account before you can log in.");
        } else {
            System.out.println("Registration failed. Ensure all fields are valid and the ID has not been used.");
        }
    }

    private void registerCareerCenterStaff() {
        System.out.print("Staff ID (e.g., abc123): ");
        String id = scanner.nextLine().trim();
        if (!STAFF_ID_PATTERN.matcher(id).matches()) {
            System.out.println("Invalid staff ID format.");
            return;
        }
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        String password = promptPasswordInput("Password (min 8 chars): ");
        if (password == null) {
            return;
        }
        System.out.print("Department: ");
        String department = scanner.nextLine().trim();
        if (!promptYesNo("Confirm staff registration? (y/n): ", true)) {
            System.out.println("Registration cancelled.");
            return;
        }
        boolean registered = userManager.registerCareerCenterStaff(id, name, password, department);
        if (registered) {
            persistStaffRecord(id, name, department);
            System.out.println("Career Center Staff registered successfully.");
        } else {
            System.out.println("Registration failed. Ensure the ID is unique and password meets requirements.");
        }
    }

    private String promptPasswordInput(String prompt) {
        System.out.print(prompt);
        String password = scanner.nextLine();
        if (password.equalsIgnoreCase("cancel")) {
            System.out.println("Registration cancelled.");
            return null;
        }
        if (password.length() < 8) {
            System.out.println("Password must be at least 8 characters long.");
            return null;
        }
        return password;
    }

    private boolean promptYesNo(String prompt, boolean defaultYes) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultYes;
            }
            if ("y".equalsIgnoreCase(input) || "yes".equalsIgnoreCase(input)) {
                return true;
            }
            if ("n".equalsIgnoreCase(input) || "no".equalsIgnoreCase(input)) {
                return false;
            }
            System.out.println("Please enter y or n.");
        }
    }

    private Integer readInt(String prompt, int min, int max, Integer defaultValue, boolean allowCancel) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty() && defaultValue != null) {
                return defaultValue;
            }
            if (allowCancel && input.equalsIgnoreCase("cancel")) {
                return null;
            }
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    throw new NumberFormatException();
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            }
        }
    }

    private int readInt(String prompt, int min, int max) {
        Integer value = readInt(prompt, min, max, null, false);
        return value == null ? min : value;
    }

    private void persistStudentRecord(String id, String name, String major, int year) {
        String line = String.join(",", id, name, major, String.valueOf(year), "");
        appendCsvLine(studentDataPath, STUDENT_HEADER, line);
    }

    private void persistCompanyRepRecord(String id, String name, String company, String department, String position) {
        String line = String.join(",", id, name, company, department, position, id, "false");
        appendCsvLine(companyDataPath, COMPANY_HEADER, line);
    }

    private void persistStaffRecord(String id, String name, String department) {
        String line = String.join(",", id, name, "Career Center Staff", department, "");
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

    public void showStudentMenu(Student student) {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=== Student Portal: " + student.getName() + " ===");
            System.out.println("1. Browse internships");
            System.out.println("2. Apply to an internship");
            System.out.println("3. View my applications");
            System.out.println("4. Request withdrawal");
            System.out.println("5. Accept an offer");
            System.out.println("6. Change password");
            System.out.println("7. Back to main menu");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> displayInternshipCatalog(student);
                case "2" -> handleStudentApplication(student);
                case "3" -> showStudentApplications(student);
                case "4" -> handleStudentWithdrawal(student);
                case "5" -> handleAcceptOffer(student);
                case "6" -> handlePasswordChange(student);
                case "7" -> exit = true;
                default -> System.out.println("Unknown option.");
            }
        }
    }

    private void displayInternshipCatalog(Student student) {
        List<Internship> internships = internshipManager.getInternships();
        if (internships.isEmpty()) {
            System.out.println("No internships available yet.");
            return;
        }
        System.out.println("\nAvailable internships:");
        int index = 1;
        for (Internship internship : internships) {
            printInternshipRow(index++, internship);
        }
    }

    private void handleStudentApplication(Student student) {
        if (student.hasAcceptedPlacement()) {
            System.out.println("You have already accepted a placement and cannot apply for new internships.");
            return;
        }
        List<Internship> available = getInternshipsOpenToStudents(student);
        if (available.isEmpty()) {
            System.out.println("No internships currently open for applications.");
            return;
        }
        Internship selection = selectInternshipFromList(available);
        if (selection == null) {
            return;
        }
        if (student.apply(selection, applicationManager)) {
            System.out.println("Application submitted for " + selection.getTitle());
        } else {
            String failure = applicationManager.getLastFailureReason();
            if (!failure.isBlank()) {
                System.out.println("Unable to apply: " + failure);
            }
        }
    }

    private void showStudentApplications(Student student) {
        List<Application> applications = student.getApplications();
        if (applications.isEmpty()) {
            System.out.println("No applications submitted yet.");
            return;
        }
        System.out.println("\nYour applications:");
        int index = 1;
        for (Application application : applications) {
            System.out.println(index++ + ". " + application.getInternship().getTitle()
                    + " (" + application.getInternship().getCompanyName() + ") - "
                    + application.getStatus());
        }
    }

    private void handleStudentWithdrawal(Student student) {
        List<Application> withdrawable = new ArrayList<>();
        for (Application application : student.getApplications()) {
            if (application.getStatus() == ApplicationStatus.PENDING
                    || application.getStatus() == ApplicationStatus.SUCCESSFUL) {
                withdrawable.add(application);
            }
        }
        if (withdrawable.isEmpty()) {
            System.out.println("No eligible applications available for withdrawal.");
            return;
        }
        Application target = selectApplicationFromList(withdrawable, "Select an application to withdraw (0 to cancel): ");
        if (target == null) {
            return;
        }
        System.out.print("Reason for withdrawal: ");
        String reason = scanner.nextLine().trim();
        try {
            WithdrawalRequest request = student.withdraw(target, withdrawalManager, reason);
            System.out.println("Withdrawal requested. Reference: " + request.getRequestedOn());
        } catch (Exception e) {
            System.out.println("Unable to request withdrawal: " + e.getMessage());
        }
    }

    private void handleAcceptOffer(Student student) {
        if (student.hasAcceptedPlacement()) {
            System.out.println("You have already accepted a placement.");
            return;
        }
        List<Application> offers = new ArrayList<>();
        for (Application application : student.getApplications()) {
            if (application.getStatus() == ApplicationStatus.SUCCESSFUL) {
                offers.add(application);
            }
        }
        if (offers.isEmpty()) {
            System.out.println("No offers available to accept at the moment.");
            return;
        }
        Application target = selectApplicationFromList(offers, "Select an application to accept (0 to cancel): ");
        if (target == null) {
            return;
        }
        try {
            student.acceptPlacement(target, applicationManager);
            System.out.println("Placement accepted for " + target.getInternship().getTitle());
        } catch (Exception e) {
            System.out.println("Unable to accept placement: " + e.getMessage());
        }
    }

    public void showRepMenu(CompanyRep rep) {
        if (!rep.isApproved()) {
            System.out.println("Account awaiting approval. Please check back later.");
            return;
        }
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=== Company Rep Dashboard: " + rep.getName() + " ===");
            System.out.println("1. View my internships");
            System.out.println("2. Create a new internship");
            System.out.println("3. Toggle internship visibility");
            System.out.println("4. Review applications");
            System.out.println("5. Change password");
            System.out.println("6. Back to main menu");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> displayRepInternships(rep);
                case "2" -> handleRepCreateInternship(rep);
                case "3" -> handleToggleVisibility(rep);
                case "4" -> handleRepReviewApplications(rep);
                case "5" -> handlePasswordChange(rep);
                case "6" -> exit = true;
                default -> System.out.println("Unknown option.");
            }
        }
    }

    private void displayRepInternships(CompanyRep rep) {
        List<Internship> mine = internshipManager.getInternshipsForRep(rep);
        if (mine.isEmpty()) {
            System.out.println("No internships submitted yet.");
            return;
        }
        System.out.println("\nYour internships:");
        int index = 1;
        for (Internship internship : mine) {
            printInternshipRow(index++, internship);
        }
    }

    private void handleRepCreateInternship(CompanyRep rep) {
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Title is required.");
            return;
        }
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        InternshipLevel level = promptInternshipLevel();
        System.out.print("Preferred major (leave blank for any): ");
        String preferredMajor = scanner.nextLine().trim();
        LocalDate openDate = readOptionalDate("Open date (yyyy-MM-dd, blank for immediate): ");
        LocalDate closeDate = readOptionalDate("Close date (yyyy-MM-dd, blank for none): ");
        int slots = readInt("Number of slots (1-10): ", 1, 10);
        try {
            Internship internship = rep.createInternship(internshipManager, title, description,
                    level, preferredMajor, openDate, closeDate, slots);
            System.out.println("Internship submitted for review: " + internship.getTitle());
        } catch (Exception e) {
            System.out.println("Unable to create internship: " + e.getMessage());
        }
    }

    private void handleToggleVisibility(CompanyRep rep) {
        List<Internship> mine = internshipManager.getInternshipsForRep(rep);
        Internship selection = selectInternshipFromList(mine);
        if (selection == null) {
            return;
        }
        boolean turnOn = promptYesNo("Turn visibility ON? (y/n): ", selection.isVisible());
        try {
            rep.toggleVisibility(internshipManager, selection, turnOn);
            System.out.println("Visibility updated for " + selection.getTitle());
        } catch (Exception e) {
            System.out.println("Unable to change visibility: " + e.getMessage());
        }
    }

    private void handleRepReviewApplications(CompanyRep rep) {
        List<Internship> mine = internshipManager.getInternshipsForRep(rep);
        Internship selection = selectInternshipFromList(mine);
        if (selection == null) {
            return;
        }
        List<Application> applications = selection.getApplications();
        if (applications.isEmpty()) {
            System.out.println("No applications submitted yet.");
            return;
        }
        System.out.println("\nApplications for " + selection.getTitle() + ":");
        int index = 1;
        for (Application application : applications) {
            System.out.println(index++ + ". " + application.getStudent().getName()
                    + " - " + application.getStatus());
        }
        if (!promptYesNo("Update an application status? (y/n): ", false)) {
            return;
        }
        Application target = selectApplicationFromList(applications, "Select application to update (0 to cancel): ");
        if (target == null) {
            return;
        }
        System.out.println("1. Pending");
        System.out.println("2. Successful");
        System.out.println("3. Unsuccessful");
        int choice = readInt("Select new status: ", 1, 3);
        ApplicationStatus status = switch (choice) {
            case 2 -> ApplicationStatus.SUCCESSFUL;
            case 3 -> ApplicationStatus.UNSUCCESSFUL;
            default -> ApplicationStatus.PENDING;
        };
        applicationManager.updateStatus(target, status);
        System.out.println("Application status updated to " + status);
    }

    public void showStaffMenu(CareerCenterStaff staff) {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=== Career Center Console: " + staff.getName() + " ===");
            System.out.println("1. Review company representative accounts");
            System.out.println("2. Review internship submissions");
            System.out.println("3. Process withdrawal requests");
            System.out.println("4. Generate reports");
            System.out.println("5. Change password");
            System.out.println("6. Back to main menu");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> reviewAccountRequests(staff);
                case "2" -> reviewInternshipSubmissions(staff);
                case "3" -> processWithdrawalRequests(staff);
                case "4" -> showReportsMenu();
                case "5" -> handlePasswordChange(staff);
                case "6" -> exit = true;
                default -> System.out.println("Unknown option.");
            }
        }
    }

    private void reviewAccountRequests(CareerCenterStaff staff) {
        List<AccountRequest> pending = userManager.getPendingAccounts(1, 100, AccountRequest.STATUS_PENDING);
        if (pending.isEmpty()) {
            System.out.println("No pending company representative requests.");
            return;
        }
        System.out.println("\nPending representative accounts:");
        for (int i = 0; i < pending.size(); i++) {
            AccountRequest request = pending.get(i);
            CompanyRep rep = request.getRep();
            System.out.println((i + 1) + ". " + rep.getName() + " (" + rep.getUserID() + ") - "
                    + rep.getCompanyName());
        }
        int choice = readInt("Select a request to process (0 to cancel): ", 0, pending.size());
        if (choice == 0) {
            return;
        }
        AccountRequest request = pending.get(choice - 1);
        if (promptYesNo("Approve this account? (y/n): ", true)) {
            staff.approveRepAccount(userManager, request);
            updateCompanyRepApproval(request.getRep().getUserID(), true);
            System.out.println("Account approved for " + request.getRep().getUserID());
        } else {
            System.out.print("Reason for rejection: ");
            String notes = scanner.nextLine().trim();
            staff.rejectRepAccount(userManager, request, notes);
            updateCompanyRepApproval(request.getRep().getUserID(), false);
            System.out.println("Account rejected.");
        }
    }

    private void reviewInternshipSubmissions(CareerCenterStaff staff) {
        List<Internship> pending = new ArrayList<>();
        for (Internship internship : internshipManager.getInternships()) {
            if (internship.getStatus() == InternshipStatus.PENDING) {
                pending.add(internship);
            }
        }
        if (pending.isEmpty()) {
            System.out.println("No pending internships to review.");
            return;
        }
        System.out.println("\nPending internships:");
        for (int i = 0; i < pending.size(); i++) {
            printInternshipRow(i + 1, pending.get(i));
        }
        int choice = readInt("Select an internship to review (0 to cancel): ", 0, pending.size());
        if (choice == 0) {
            return;
        }
        Internship target = pending.get(choice - 1);
        if (promptYesNo("Approve this internship? (y/n): ", true)) {
            staff.approveInternship(internshipManager, target);
            System.out.println("Internship approved.");
        } else {
            staff.rejectInternship(internshipManager, target);
            System.out.println("Internship rejected.");
        }
    }

    private void processWithdrawalRequests(CareerCenterStaff staff) {
        List<WithdrawalRequest> requests = withdrawalManager.getPendingRequests();
        if (requests.isEmpty()) {
            System.out.println("No withdrawal requests pending.");
            return;
        }
        System.out.println("\nWithdrawal requests:");
        for (int i = 0; i < requests.size(); i++) {
            WithdrawalRequest request = requests.get(i);
            System.out.println((i + 1) + ". " + request.getStudent().getName()
                    + " - " + request.getApplication().getInternship().getTitle()
                    + " | Reason: " + request.getReason());
        }
        int choice = readInt("Select a request to process (0 to cancel): ", 0, requests.size());
        if (choice == 0) {
            return;
        }
        WithdrawalRequest target = requests.get(choice - 1);
        boolean approve = promptYesNo("Approve this withdrawal? (y/n): ", true);
        staff.processWithdrawal(withdrawalManager, target, approve);
        System.out.println("Withdrawal request processed.");
    }

    private void showReportsMenu() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=== Reports ===");
            System.out.println("1. By status");
            System.out.println("2. By preferred major");
            System.out.println("3. By internship level");
            System.out.println("4. Company summary");
            System.out.println("5. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> runStatusReport();
                case "2" -> runMajorReport();
                case "3" -> runLevelReport();
                case "4" -> runCompanyReport();
                case "5" -> exit = true;
                default -> System.out.println("Unknown option.");
            }
        }
    }

    private void runStatusReport() {
        InternshipStatus status = promptStatusSelection();
        reportGenerator.generateByStatus(internshipManager.getInternships(), status);
    }

    private void runMajorReport() {
        System.out.print("Preferred major: ");
        String major = scanner.nextLine().trim();
        if (major.isEmpty()) {
            System.out.println("Major cannot be empty.");
            return;
        }
        reportGenerator.generateByMajor(internshipManager.getInternships(), major);
    }

    private void runLevelReport() {
        InternshipLevel level = promptInternshipLevel();
        reportGenerator.generateByLevel(internshipManager.getInternships(), level);
    }

    private void runCompanyReport() {
        System.out.print("Company name: ");
        String company = scanner.nextLine().trim();
        if (company.isEmpty()) {
            System.out.println("Company cannot be empty.");
            return;
        }
        reportGenerator.generateCompanySummary(internshipManager.getInternships(), company);
    }

    private List<Internship> getInternshipsOpenToStudents(Student student) {
        List<Internship> available = new ArrayList<>();
        String studentMajor = student.getMajor();
        int studentYear = student.getYearOfStudy();
        for (Internship internship : internshipManager.getInternships()) {
            if (internship.getStatus() != InternshipStatus.APPROVED || !internship.isVisible() || internship.isFull()) {
                continue;
            }
            String preferredMajor = internship.getPreferredMajor();
            if (preferredMajor != null && !preferredMajor.isBlank()
                    && studentMajor != null && !preferredMajor.equalsIgnoreCase(studentMajor)) {
                continue;
            }
            InternshipLevel level = internship.getLevel();
            if (studentYear <= 2 && level != null && level != InternshipLevel.BASIC) {
                continue;
            }
            available.add(internship);
        }
        return available;
    }

    private Internship selectInternshipFromList(List<Internship> internships) {
        if (internships == null || internships.isEmpty()) {
            System.out.println("No internships to select.");
            return null;
        }
        for (int i = 0; i < internships.size(); i++) {
            printInternshipRow(i + 1, internships.get(i));
        }
        int choice = readInt("Select an internship (0 to cancel): ", 0, internships.size());
        if (choice == 0) {
            return null;
        }
        return internships.get(choice - 1);
    }

    private Application selectApplicationFromList(List<Application> applications, String prompt) {
        if (applications == null || applications.isEmpty()) {
            System.out.println("No applications available.");
            return null;
        }
        for (int i = 0; i < applications.size(); i++) {
            Application app = applications.get(i);
            System.out.println((i + 1) + ". " + app.getInternship().getTitle()
                    + " - " + app.getStatus());
        }
        int choice = readInt(prompt, 0, applications.size());
        if (choice == 0) {
            return null;
        }
        return applications.get(choice - 1);
    }

    private void handlePasswordChange(User user) {
        while (true) {
            String newPassword = readPassword("New password (min 8 chars, type 'cancel' to exit)");
            if ("cancel".equalsIgnoreCase(newPassword)) {
                System.out.println("Password change cancelled.");
                return;
            }
            String confirm = readPassword("Confirm new password");
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

    private LocalDate readOptionalDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use yyyy-MM-dd.");
            }
        }
    }

    private InternshipLevel promptInternshipLevel() {
        InternshipLevel[] levels = InternshipLevel.values();
        for (int i = 0; i < levels.length; i++) {
            System.out.println((i + 1) + ". " + levels[i]);
        }
        int choice = readInt("Select internship level: ", 1, levels.length);
        return levels[choice - 1];
    }

    private InternshipStatus promptStatusSelection() {
        InternshipStatus[] statuses = InternshipStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i]);
        }
        int choice = readInt("Select a status: ", 1, statuses.length);
        return statuses[choice - 1];
    }

    private void printInternshipRow(int index, Internship internship) {
        int totalSlots = internship.getSlots().size();
        long filledSlots = internship.getSlots().stream().filter(slot -> slot.getAssignedStudent() != null).count();
        System.out.println(index + ". " + internship.getTitle() + " (" + internship.getCompanyName() + ")"
                + " | Status: " + internship.getStatus()
                + " | Level: " + internship.getLevel()
                + " | Major: " + (internship.getPreferredMajor() == null ? "Any" : internship.getPreferredMajor())
                + " | Visibility: " + (internship.isVisible() ? "On" : "Off")
                + " | Slots: " + filledSlots + "/" + totalSlots);
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
