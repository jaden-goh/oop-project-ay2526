package modular.controller;

import modular.model.ModApplication;
import modular.model.ModCompanyRep;
import modular.model.ModInternship;
import modular.service.ModCompanyRepService;

import java.util.List;
import java.util.Scanner;

public class ModCompanyRepMenuController {
    private final ModCompanyRepService repService;
    private final List<ModInternship> internships;

    public ModCompanyRepMenuController(ModCompanyRepService repService, List<ModInternship> internships) {
        this.repService = repService;
        this.internships = internships;
    }

    public void displayMenu(ModCompanyRep rep, Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("=== Company Rep Menu (Modular) ===");
            System.out.println("1. Create internship");
            System.out.println("2. Manage applications");
            System.out.println("3. Logout");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    ModInternship created = repService.createInternship(rep, scanner);
                    if (created != null && !internships.contains(created)) {
                        internships.add(created);
                    }
                }
                case "2" -> manageApplications(rep, scanner);
                case "3" -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void manageApplications(ModCompanyRep rep, Scanner scanner) {
        if (rep.getInternships().isEmpty()) {
            System.out.println("No internships created.");
            return;
        }
        for (int i = 0; i < rep.getInternships().size(); i++) {
            ModInternship internship = rep.getInternships().get(i);
            System.out.printf("%d. %s (%s)%n", i + 1, internship.getTitle(), internship.getStatus());
        }
        System.out.print("Select internship: ");
        int idx;
        try {
            idx = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (idx < 1 || idx > rep.getInternships().size()) {
            System.out.println("Invalid selection.");
            return;
        }
        ModInternship selected = rep.getInternships().get(idx - 1);
        List<ModApplication> apps = repService.viewApplications(rep, selected);
        if (apps.isEmpty()) {
            System.out.println("No applications to manage.");
            return;
        }
        for (int i = 0; i < apps.size(); i++) {
            ModApplication app = apps.get(i);
            System.out.printf("%d. %s (%s)%n", i + 1, app.getStudent().getName(), app.getStatus());
        }
        System.out.print("Select application: ");
        int appIdx;
        try {
            appIdx = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (appIdx < 1 || appIdx > apps.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        ModApplication target = apps.get(appIdx - 1);
        System.out.print("Approve (A) / Reject (R): ");
        String decision = scanner.nextLine().trim().toUpperCase();
        switch (decision) {
            case "A" -> System.out.println(repService.approveApplication(rep, target) ? "Approved." : "Unable to approve.");
            case "R" -> System.out.println(repService.rejectApplication(rep, target) ? "Rejected." : "Unable to reject.");
            default -> System.out.println("Invalid action.");
        }
    }
}
