package modular.controller;

import modular.model.ModCareerCenterStaff;
import modular.model.ModCompanyRep;
import modular.model.ModInternship;
import modular.model.ModUser;
import modular.service.ModCareerCenterStaffService;

import java.util.List;
import java.util.Scanner;

public class ModCareerStaffMenuController {
    private final ModCareerCenterStaffService staffService;
    private final List<ModInternship> internships;
    private final List<ModUser> users;

    public ModCareerStaffMenuController(ModCareerCenterStaffService staffService,
                                        List<ModInternship> internships,
                                        List<ModUser> users) {
        this.staffService = staffService;
        this.internships = internships;
        this.users = users;
    }

    public void displayMenu(ModCareerCenterStaff staff, Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("=== Career Staff Menu (Modular) ===");
            System.out.println("1. Manage internship requests");
            System.out.println("2. Manage withdrawal requests");
            System.out.println("3. Approve company representatives");
            System.out.println("4. Logout");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> staffService.handleInternshipRequests(internships, scanner);
                case "2" -> staffService.handleWithdrawalRequests(users, scanner);
                case "3" -> handleRepApprovals(scanner);
                case "4" -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void handleRepApprovals(Scanner scanner) {
        List<ModCompanyRep> pending = users.stream()
                .filter(ModCompanyRep.class::isInstance)
                .map(ModCompanyRep.class::cast)
                .filter(rep -> !rep.isAuthorised())
                .toList();
        if (pending.isEmpty()) {
            System.out.println("No pending reps.");
            return;
        }
        for (int i = 0; i < pending.size(); i++) {
            ModCompanyRep rep = pending.get(i);
            System.out.printf("%d. %s (%s)%n", i + 1, rep.getName(), rep.getEmail());
        }
        System.out.print("Select rep: ");
        int idx;
        try {
            idx = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (idx < 1 || idx > pending.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        System.out.print("Approve? (y/n): ");
        boolean approve = scanner.nextLine().trim().equalsIgnoreCase("y");
        staffService.authoriseRep(pending.get(idx - 1), approve);
    }
}
