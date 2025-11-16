package modular.service;

import modular.model.ModApplication;
import modular.model.ModApplicationStatus;
import modular.model.ModCompanyRep;
import modular.model.ModInternship;
import modular.model.ModInternshipSlot;
import modular.model.ModInternshipStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class ModCompanyRepService {

    public ModInternship createInternship(ModCompanyRep rep, Scanner scanner) {
        if (rep == null || rep.getCompany() == null) {
            System.out.println("Link to a company before creating internships.");
            return null;
        }
        if (rep.getInternships().size() >= 5) {
            System.out.println("Maximum internships created.");
            return null;
        }
        System.out.println("=== Create Internship (Modular) ===");
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        System.out.print("Level (Basic/Intermediate/Advanced): ");
        String level = scanner.nextLine().trim();
        System.out.print("Preferred Major: ");
        String major = scanner.nextLine().trim();
        Date openDate = new Date();
        Date closeDate = new Date(openDate.getTime() + 7L * 24 * 60 * 60 * 1000);
        System.out.print("Number of slots (max 10): ");
        int slots;
        try {
            slots = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid slot count.");
            return null;
        }
        if (slots <= 0 || slots > 10) {
            System.out.println("Slot count must be 1-10.");
            return null;
        }
        ModInternship internship = new ModInternship();
        internship.setTitle(title);
        internship.setDescription(description);
        internship.setLevel(level);
        internship.setPreferredMajor(major);
        internship.setOpenDate(openDate);
        internship.setCloseDate(closeDate);
        internship.setCompany(rep.getCompany());
        internship.setRep(rep);
        internship.setStatus(ModInternshipStatus.PENDING);
        for (int i = 1; i <= slots; i++) {
            internship.addSlot(new ModInternshipSlot(i));
        }
        rep.getInternships().add(internship);
        rep.getCompany().addInternship(internship);
        System.out.println("Internship created (pending approval).");
        return internship;
    }

    public List<ModApplication> viewApplications(ModCompanyRep rep, ModInternship internship) {
        if (rep == null || internship == null) {
            return List.of();
        }
        if (!rep.getInternships().contains(internship)) {
            return List.of();
        }
        List<ModApplication> apps = new ArrayList<>();
        for (ModInternshipSlot slot : internship.getSlots()) {
            if (slot.getApplication() != null) {
                apps.add(slot.getApplication());
            }
        }
        return apps;
    }

    public boolean approveApplication(ModCompanyRep rep, ModApplication application) {
        if (rep == null || application == null) return false;
        ModInternship internship = application.getInternship();
        if (internship == null || !rep.getInternships().contains(internship)) {
            return false;
        }
        if (internship.isFull()) {
            System.out.println("Internship full.");
            return false;
        }
        application.setStatus(ModApplicationStatus.SUCCESSFUL);
        for (ModInternshipSlot slot : internship.getSlots()) {
            if (!slot.isFilled()) {
                slot.setApplication(application);
                break;
            }
        }
        if (internship.isFull()) {
            internship.setStatus(ModInternshipStatus.FILLED);
        }
        return true;
    }

    public boolean rejectApplication(ModCompanyRep rep, ModApplication application) {
        if (rep == null || application == null) return false;
        ModInternship internship = application.getInternship();
        if (internship == null || !rep.getInternships().contains(internship)) {
            return false;
        }
        application.setStatus(ModApplicationStatus.UNSUCCESSFUL);
        return true;
    }
}
