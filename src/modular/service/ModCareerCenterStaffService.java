package modular.service;

import modular.model.ModApplication;
import modular.model.ModApplicationStatus;
import modular.model.ModCareerCenterStaff;
import modular.model.ModCompanyRep;
import modular.model.ModInternship;
import modular.model.ModInternshipStatus;
import modular.model.ModStudent;
import modular.model.ModUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ModCareerCenterStaffService {

    public boolean authoriseRep(ModCompanyRep rep, boolean approve) {
        if (rep == null) {
            System.out.println("No representative selected.");
            return false;
        }
        rep.setAuthorised(approve);
        System.out.println(rep.getName() + (approve ? " approved." : " rejected."));
        return approve;
    }

    public boolean approveInternship(ModInternship internship, boolean approve) {
        if (internship == null) return false;
        internship.setStatus(approve ? ModInternshipStatus.APPROVED : ModInternshipStatus.REJECTED);
        internship.setApproved(approve);
        internship.setVisible(approve);
        return approve;
    }

    public void handleInternshipRequests(List<ModInternship> internships, Scanner scanner) {
        if (internships == null || internships.isEmpty()) {
            System.out.println("No internships to review.");
            return;
        }
        ArrayList<ModInternship> pending = new ArrayList<>();
        for (ModInternship internship : internships) {
            if (internship.getStatus() == ModInternshipStatus.PENDING) {
                pending.add(internship);
            }
        }
        if (pending.isEmpty()) {
            System.out.println("No pending requests.");
            return;
        }
        for (int i = 0; i < pending.size(); i++) {
            ModInternship in = pending.get(i);
            String company = in.getCompany() != null ? in.getCompany().getCompanyName() : "Unknown";
            System.out.printf("%d. %s (%s)%n", i + 1, in.getTitle(), company);
        }
        System.out.print("Select internship to review: ");
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
        System.out.print("Approve internship? (y/n): ");
        boolean approve = scanner.nextLine().trim().equalsIgnoreCase("y");
        approveInternship(pending.get(idx - 1), approve);
    }

    public void handleWithdrawalRequests(List<ModUser> users, Scanner scanner) {
        ArrayList<ModApplication> pending = new ArrayList<>();
        for (ModUser user : users) {
            if (user instanceof ModStudent student) {
                for (ModApplication app : student.getApplications()) {
                    if (app.getStatus() == ModApplicationStatus.PENDING_WITHDRAWAL) {
                        pending.add(app);
                    }
                }
            }
        }
        if (pending.isEmpty()) {
            System.out.println("No withdrawal requests.");
            return;
        }
        for (int i = 0; i < pending.size(); i++) {
            ModApplication app = pending.get(i);
            System.out.printf("%d. %s - %s%n", i + 1,
                    app.getStudent().getName(),
                    app.getInternship().getTitle());
        }
        System.out.print("Select request: ");
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
        System.out.print("Approve withdrawal? (y/n): ");
        boolean approve = scanner.nextLine().trim().equalsIgnoreCase("y");
        ModApplication target = pending.get(idx - 1);
        if (approve) {
            target.setStatus(ModApplicationStatus.WITHDRAWN);
            target.setWithdrawn(true);
            System.out.println("Withdrawal approved.");
        } else {
            target.setStatus(ModApplicationStatus.PENDING);
            target.setWithdrawn(false);
            System.out.println("Withdrawal rejected.");
        }
    }
}
