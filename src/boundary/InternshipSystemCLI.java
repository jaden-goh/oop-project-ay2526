package boundary;
import control.UserDataLoader;
import entity.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class InternshipSystemCLI {
    //private attributes
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^U\\d{7}[A-Za-z]$");// U, 7 digits, 1 letter
    private static final String COMPANY_REP_CSV = "data/sample_company_representative_list.csv";
    private static final String COMPANY_REP_HEADER = "CompanyRepID,Name,CompanyName,Department,Position,Email,Approved";
    private ArrayList<User> users = new ArrayList<>();
    private User currentUser;;
    private final Scanner scanner = new Scanner(System.in);
    private final UserDataLoader loader = new UserDataLoader();

    //getters and setters
    public ArrayList<User> getUsers() { return users; }
    public void setUsers(ArrayList<User> users) { this.users = users; }
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }

    public void loadInitialData() {
        List<Student> students = loader.loadStudents();
        List<CareerCenterStaff> staff = loader.loadStaff();
        List<CompanyRep> reps = loader.loadApprovedCompanyReps();
        users.addAll(students);
        users.addAll(staff);
        users.addAll(reps);
    }
    public void displayStudentMenu() { }
    public void displayRepMenu() { }
    public void displayStaffMenu() { }

    public boolean register() { return false; }
    public Internship createInternship(String title, String description, String level, String preferredMajor,
                                       java.util.Date openDate, java.util.Date closeDate, int numSlots) { return null; }
    public void toggleVisibility(Internship internship) { }
    public boolean approveApplication(Application application) { return false; }
    public boolean rejectApplication(Application application) { return false; }
    public java.util.List<Application> viewApplications(Internship internship) { return null; }

    private void handleStudentLogin() {
        System.out.print("Enter Student ID: ");
        String id = scanner.nextLine().trim();
        if (!STUDENT_ID_PATTERN.matcher(id).matches()) {
            System.out.println("Invalid student ID.");
            return;
        }
        User user = findUserById(id, Student.class);
        if (user == null) {
            System.out.println("Student not found.");
            return;
        }
        currentUser = user;
        System.out.println("Welcome, " + user.getName());
        displayStudentMenu();
        currentUser = null;
    }

    private void handleCompanyRepLogin() {
        System.out.print("Enter Company Rep email: ");
        String email = scanner.nextLine().trim();
        if (!email.endsWith(".com")) {
            System.out.println("Invalid company email format.");
            return;
        }
        String[] record = getCompanyRepRecord(email);
        if (record == null) {
            record = registerCompanyRep(email);
            if (record == null) {
                System.out.println("Unable to register company representative at this time.");
                return;
            }
            System.out.println("Registration submitted. Await approval from Career Center Staff.");
            return;
        }
        if (!isApprovedStatus(record[6])) {
            System.out.println("Your account is pending approval. Please wait for Career Center Staff to approve your registration.");
            return;
        }
        CompanyRep rep = ensureCompanyRepUser(record);
        if (rep == null) {
            System.out.println("Unable to load company representative profile.");
            return;
        }
        rep.setAuthorised(true);
        currentUser = rep;
        System.out.println("Welcome, " + rep.getName());
        displayRepMenu();
        currentUser = null;
    }

    private void handleCareerStaffLogin() {
        System.out.print("Enter Career Staff ID: ");
        String email = scanner.nextLine().trim();
        if (!email.endsWith("@ntu.edu.sg")) {
            System.out.println("Invalid email format for career staff.");
            return;
        }
        User user = findUserById(email, CareerCenterStaff.class);
        if (user == null) {
            System.out.println("Career staff not found.");
            return;
        }
        currentUser = user;
        System.out.println("Welcome, " + user.getName());
        displayStaffMenu();
        currentUser = null;
    }

    private User findUserById(String id, Class<? extends User> type) { // checks if user is of correct type
        for (User user : users) {
            if (type.isInstance(user) && user.getId().equalsIgnoreCase(id)) {
                return user;
            }
        }
        return null;
    }

    private String[] getCompanyRepRecord(String email) { // read from CSV
        Path path = Paths.get(COMPANY_REP_CSV);
        if (!Files.exists(path)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] columns = line.split(",", -1);
                if (columns.length < 7) continue;
                if (columns[5].equalsIgnoreCase(email)) {
                    return columns;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to read company representative list: " + e.getMessage());
        }
        return null;
    }

    private String[] registerCompanyRep(String email) { // first time registration
        System.out.println("No existing company representative record found.");
        System.out.println("Please provide details for approval.");
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Company Name: ");
        String company = scanner.nextLine().trim();
        System.out.print("Department: ");
        String department = scanner.nextLine().trim();
        System.out.print("Position: ");
        String position = scanner.nextLine().trim();

        if (name.isEmpty() || company.isEmpty()) {
            System.out.println("Name and company are required.");
            return null;
        }

        String[] record = new String[]{
                generateCompanyRepId(),
                name,
                company,
                department,
                position,
                email,
                "false"
        };

        if (appendCompanyRepRecord(record)) {
            return record;
        }
        return null;
    }

    private String generateCompanyRepId() {
        return "REP-" + System.currentTimeMillis(); //Generates unique ID based on timestamp
    }

    private boolean appendCompanyRepRecord(String[] record) { // append to CSV
        Path path = Paths.get(COMPANY_REP_CSV);
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path) || Files.size(path) == 0) {
                Files.writeString(path, COMPANY_REP_HEADER + System.lineSeparator());
            }
            Files.writeString(path, String.join(",", record) + System.lineSeparator(), StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.out.println("Failed to save company representative registration: " + e.getMessage());
            return false;
        }
    }

    private CompanyRep ensureCompanyRepUser(String[] record) { // load or create CompanyRep user
        String email = record[5];
        User existing = findUserById(email, CompanyRep.class);
        if (existing != null) {
            return (CompanyRep) existing;
        }
        CompanyRep rep = new CompanyRep(email, record[1], "");
        rep.setAuthorised(isApprovedStatus(record[6]));
        users.add(rep);
        return rep;
    }

    private boolean isApprovedStatus(String status) {
        return "true".equalsIgnoreCase(status) || "approved".equalsIgnoreCase(status);
    }

    public static void main(String[] args){
        InternshipSystemCLI cli = new InternshipSystemCLI();
        cli.loadInitialData();
        System.out.println("Welcome to the Internship Management System.");
        boolean running = true;
        while (running) {
            System.out.println("\nSelect user type:");
            System.out.println("1. Student");
            System.out.println("2. Company Representative");
            System.out.println("3. Career Center Staff");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            String choice = cli.scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    cli.handleStudentLogin();
                    break;
                case "2":
                    cli.handleCompanyRepLogin();
                    break;
                case "3":
                    cli.handleCareerStaffLogin();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
        System.out.println("Goodbye.");
    }
}
