package boundary;
import control.InternshipManager;
import entity.*;
import java.util.ArrayList;
import java.util.Scanner;

public class InternshipSystemCLI {
    private ArrayList<User> users = new ArrayList<>();
    private User currentUser;
    private InternshipManager manager = new InternshipManager();
    private ArrayList<Internship> internships = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);

    public ArrayList<User> getUsers() { return users; }
    public void setUsers(ArrayList<User> users) { this.users = users; }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }

    public InternshipManager getManager() { return manager; }
    public void setManager(InternshipManager manager) { this.manager = manager; }

    public ArrayList<Internship> getInternships() { return internships; }
    public void setInternships(ArrayList<Internship> internships) { this.internships = internships; }

    // functions
    public void run() {
        System.out.println("Welcome to the Internship Management System CLI");
        displayLoginMenu();
    }
    public void displayLoginMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\nSelect user type:");
            System.out.println("1. Student");
            System.out.println("2. Company Representative");
            System.out.println("3. Career Center Staff");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleStudentLogin();
                    break;
                case "2":
                    handleCompanyRepLogin();
                    break;
                case "3":
                    handleCareerStaffLogin();
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
        if (!id.matches("^U\\d{7}[A-Za-z]$")) {// U, 7 digits, 1 letter
            System.out.println("Invalid student ID format.");
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
        if (!email.matches("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$") || email.endsWith("@ntu.edu.sg")) {
            System.out.println("Invalid company email format.");
            return;
        }
        User user = findUserById(email, CompanyRep.class);
        if (user == null) {
            System.out.println("Company representative not found.");
            return;
        }
        currentUser = user;
        System.out.println("Welcome, " + user.getName());
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

    private User findUserById(String id, Class<? extends User> type) {
        for (User user : users) {
            if (type.isInstance(user) && user.getId().equalsIgnoreCase(id)) {
                return user;
            }
        }
        return null;
    }

    public static void main(String[] args){
        InternshipSystemCLI cli = new InternshipSystemCLI();
        cli.run();
    }
}
