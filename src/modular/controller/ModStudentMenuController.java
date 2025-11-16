package modular.controller;

import modular.model.ModInternship;
import modular.model.ModStudent;
import modular.service.ModStudentApplicationService;

import java.util.List;
import java.util.Scanner;

public class ModStudentMenuController {
    private final ModStudentApplicationService applicationService;
    private final List<ModInternship> internships;

    public ModStudentMenuController(ModStudentApplicationService applicationService,
                                    List<ModInternship> internships) {
        this.applicationService = applicationService;
        this.internships = internships;
    }

    public void displayMenu(ModStudent student, Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("=== Student Menu (Modular) ===");
            System.out.println("1. View internships");
            System.out.println("2. Apply for internship");
            System.out.println("3. View applications");
            System.out.println("4. Logout");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> listInternships();
                case "2" -> handleApplication(student, scanner);
                case "3" -> viewApplications(student);
                case "4" -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void listInternships() {
        if (internships.isEmpty()) {
            System.out.println("No internships available.");
            return;
        }
        for (int i = 0; i < internships.size(); i++) {
            ModInternship in = internships.get(i);
            String company = in.getCompany() != null ? in.getCompany().getCompanyName() : "Unknown";
            System.out.printf("%d. %s at %s [%s]%n",
                    i + 1,
                    in.getTitle(),
                    company,
                    in.getLevel());
        }
    }

    private void handleApplication(ModStudent student, Scanner scanner) {
        listInternships();
        if (internships.isEmpty()) {
            return;
        }
        System.out.print("Select internship: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice < 1 || choice > internships.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            ModInternship target = internships.get(choice - 1);
            if (applicationService.apply(student, target)) {
                System.out.println("Application submitted.");
            } else {
                System.out.println("Unable to submit application.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        }
    }

    private void viewApplications(ModStudent student) {
        if (student.getApplications().isEmpty()) {
            System.out.println("No applications submitted.");
            return;
        }
        for (int i = 0; i < student.getApplications().size(); i++) {
            var app = student.getApplications().get(i);
            System.out.printf("%d. %s - %s%n",
                    i + 1,
                    app.getInternship().getTitle(),
                    app.getStatus());
        }
    }
}
