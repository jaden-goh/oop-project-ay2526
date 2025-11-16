package boundary;

import modular.controller.ModCareerStaffMenuController;
import modular.controller.ModCompanyRepMenuController;
import modular.controller.ModStudentMenuController;
import modular.model.ModCareerCenterStaff;
import modular.model.ModCompanyRep;
import modular.model.ModInternship;
import modular.model.ModStudent;
import modular.model.ModUser;
import modular.repository.ModCareerStaffRepository;
import modular.repository.ModCompanyRepRepository;
import modular.repository.ModStudentRepository;
import modular.service.ModAuthenticationService;
import modular.service.ModCareerCenterStaffService;
import modular.service.ModCompanyRepService;
import modular.service.ModRegistrationService;
import modular.service.ModStudentApplicationService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Stand-alone CLI that relies exclusively on the modular domain/services.
 * Legacy classes remain untouched.
 */
public class ModularInternshipSystemCLI {
    private final List<ModUser> users = new ArrayList<>();
    private final List<ModInternship> internships = new ArrayList<>();
    private ModUser currentUser;
    private final Scanner scanner = new Scanner(System.in);

    private final ModularUserAuthenticator authenticator;
    private final ModStudentMenuController studentController;
    private final ModCompanyRepMenuController repController;
    private final ModCareerStaffMenuController staffController;

    private final ModStudentRepository studentRepository;
    private final ModCompanyRepRepository companyRepRepository;
    private final ModCareerStaffRepository staffRepository;

    public ModularInternshipSystemCLI() {
        this(
                Path.of("data/modular_students.csv"),
                Path.of("data/modular_company_reps.csv"),
                Path.of("data/modular_staff.csv")
        );
    }

    public ModularInternshipSystemCLI(Path studentCsv, Path repCsv, Path staffCsv) {
        this.studentRepository = new ModStudentRepository(studentCsv);
        this.companyRepRepository = new ModCompanyRepRepository(repCsv);
        this.staffRepository = new ModCareerStaffRepository(staffCsv);

        ModAuthenticationService authenticationService = new ModAuthenticationService(users);
        ModRegistrationService registrationService = new ModRegistrationService(
                studentRepository,
                companyRepRepository,
                staffRepository,
                users
        );

        this.authenticator = new ModularUserAuthenticator(scanner, authenticationService, registrationService);
        this.studentController = new ModStudentMenuController(new ModStudentApplicationService(), internships);
        this.repController = new ModCompanyRepMenuController(new ModCompanyRepService(), internships);
        this.staffController = new ModCareerStaffMenuController(
                new ModCareerCenterStaffService(),
                internships,
                users
        );
    }

    public void loadInitialData() {
        users.clear();
        users.addAll(studentRepository.loadAll());
        users.addAll(staffRepository.loadAll());
        users.addAll(companyRepRepository.loadAll());
    }

    public void run() {
        System.out.println("Welcome to the Modular Internship Management System.");
        boolean running = true;
        while (running) {
            System.out.println("\nSelect user type:");
            System.out.println("1. Student");
            System.out.println("2. Company Representative");
            System.out.println("3. Career Center Staff");
            System.out.println("4. Register a new user");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> handleStudentFlow();
                case "2" -> handleCompanyRepFlow();
                case "3" -> handleStaffFlow();
                case "4" -> authenticator.register();
                case "0" -> running = false;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
        System.out.println("Goodbye.");
    }

    private void handleStudentFlow() {
        ModStudent student = authenticator.handleStudentLogin();
        if (student == null) return;
        currentUser = student;
        System.out.println("Welcome, " + student.getName());
        studentController.displayMenu(student, scanner);
        currentUser = null;
    }

    private void handleCompanyRepFlow() {
        ModCompanyRep rep = authenticator.handleCompanyRepLogin();
        if (rep == null) return;
        currentUser = rep;
        System.out.println("Welcome, " + rep.getName());
        repController.displayMenu(rep, scanner);
        currentUser = null;
    }

    private void handleStaffFlow() {
        ModCareerCenterStaff staff = authenticator.handleCareerStaffLogin();
        if (staff == null) return;
        currentUser = staff;
        System.out.println("Welcome, " + staff.getName());
        staffController.displayMenu(staff, scanner);
        currentUser = null;
    }

    public static void main(String[] args) {
        ModularInternshipSystemCLI cli = new ModularInternshipSystemCLI();
        cli.loadInitialData();
        cli.run();
    }
}
