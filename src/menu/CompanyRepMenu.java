package menu;

import boundary.ConsoleHelper;
import boundary.InternshipBrowser;
import control.ApplicationManager;
import control.InternshipManager;
import control.NotificationManager;
import control.UserManager;
import entity.Application;
import entity.ApplicationStatus;
import entity.CompanyRep;
import entity.Internship;
import entity.InternshipLevel;
import entity.InternshipStatus;
import entity.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class CompanyRepMenu {
    private final ConsoleHelper console;
    private final InternshipBrowser internshipBrowser;
    private final InternshipManager internshipManager;
    private final ApplicationManager applicationManager;
    private final NotificationManager notificationManager;
    private final UserManager userManager;
    private final Consumer<User> notificationDisplay;
    private final Consumer<User> passwordChanger;

    public CompanyRepMenu(ConsoleHelper console,
                          InternshipBrowser internshipBrowser,
                          InternshipManager internshipManager,
                          ApplicationManager applicationManager,
                          NotificationManager notificationManager,
                          UserManager userManager,
                          Consumer<User> notificationDisplay,
                          Consumer<User> passwordChanger) {
        this.console = console;
        this.internshipBrowser = internshipBrowser;
        this.internshipManager = internshipManager;
        this.applicationManager = applicationManager;
        this.notificationManager = notificationManager;
        this.userManager = userManager;
        this.notificationDisplay = notificationDisplay;
        this.passwordChanger = passwordChanger;
    }

    public void show(CompanyRep rep) {
        if (!rep.isApproved()) {
            System.out.println("Account awaiting approval. Please check back later.");
            return;
        }
        boolean exit = false;
        while (!exit) {
            notificationDisplay.accept(rep);
            System.out.println("\n=== Company Rep Dashboard: " + rep.getName() + " ===");
            System.out.println("1. Manage my internships");
            System.out.println("2. Create a new internship");
            System.out.println("3. Toggle internship visibility");
            System.out.println("4. Review applications");
            System.out.println("5. Change password");
            System.out.println("6. Back to main menu");
            String choice = console.readLine("Choice: ");
            switch (choice) {
                case "1" -> manageRepInternships(rep);
                case "2" -> handleRepCreateInternship(rep);
                case "3" -> handleToggleVisibility(rep);
                case "4" -> handleRepReviewApplications(rep);
                case "5" -> passwordChanger.accept(rep);
                case "6" -> exit = true;
                default -> System.out.println("Unknown option.");
            }
        }
    }

    private void manageRepInternships(CompanyRep rep) {
        List<Internship> mine = internshipBrowser.fetchFilteredInternships(
                rep, internship -> internship.getRepInCharge() == rep);
        if (mine.isEmpty()) {
            System.out.println("No internships submitted yet.");
            return;
        }
        System.out.println("\nYour internships:");
        for (int i = 0; i < mine.size(); i++) {
            console.printInternshipRow(i + 1, mine.get(i));
        }
        if (!console.promptYesNo("Delete internships from this list? (y/n): ", false)) {
            return;
        }
        String selection = console.readLine("Enter numbers to delete (comma separated, 0 to cancel): ");
        if (selection.isBlank() || "0".equals(selection.trim())) {
            System.out.println("Deletion cancelled.");
            return;
        }
        String[] tokens = selection.split(",");
        Set<Integer> indexes = new HashSet<>();
        for (String token : tokens) {
            try {
                int value = Integer.parseInt(token.trim());
                if (value < 1 || value > mine.size()) {
                    System.out.println("Ignoring invalid selection: " + token.trim());
                    continue;
                }
                indexes.add(value - 1);
            } catch (NumberFormatException e) {
                System.out.println("Ignoring invalid selection: " + token.trim());
            }
        }
        if (indexes.isEmpty()) {
            System.out.println("No valid selections to delete.");
            return;
        }
        List<Internship> toRemove = new ArrayList<>();
        for (Integer idx : indexes) {
            toRemove.add(mine.get(idx));
        }
        if (!console.promptYesNo("Confirm deletion of " + toRemove.size() + " internship(s)? (y/n): ", false)) {
            System.out.println("Deletion cancelled.");
            return;
        }
        internshipManager.removeInternships(rep, toRemove);
        System.out.println(toRemove.size() + " internship(s) removed.");
    }

    private void handleRepCreateInternship(CompanyRep rep) {
        String title = console.readLine("Title: ");
        if (title.isEmpty()) {
            System.out.println("Title is required.");
            return;
        }
        String description = console.readLine("Description: ");
        InternshipLevel level = console.promptInternshipLevel();
        String preferredMajor = console.promptPreferredMajorSelection();
        LocalDate openDate = console.readOptionalDate("Open date (yyyy-MM-dd, blank for immediate): ");
        LocalDate closeDate = promptValidCloseDate(openDate);
        int slots = console.readInt("Number of slots (1-10): ", 1, 10);
        try {
            Internship internship = rep.createInternship(internshipManager, title, description,
                    level, preferredMajor, openDate, closeDate, slots);
            System.out.println("Internship submitted for review: " + internship.getTitle());
            notificationManager.notifyStaffInternshipSubmission(
                    userManager.getCareerCenterStaffMembers(), internship);
        } catch (Exception e) {
            System.out.println("Unable to create internship: " + e.getMessage());
        }
    }

    private void handleToggleVisibility(CompanyRep rep) {
        List<Internship> mine = internshipManager.getInternshipsForRep(rep);
        Internship selection = console.selectInternshipFromList(mine);
        if (selection == null) {
            return;
        }
        if (selection.getStatus() == InternshipStatus.PENDING) {
            System.out.println("Visibility cannot be changed while the internship is pending approval.");
            return;
        }
        boolean turnOn = console.promptYesNo("Turn visibility ON? (y/n): ", selection.isVisible());
        try {
            rep.toggleVisibility(internshipManager, selection, turnOn);
            System.out.println("Visibility updated for " + selection.getTitle());
        } catch (Exception e) {
            System.out.println("Unable to change visibility: " + e.getMessage());
        }
    }

    private void handleRepReviewApplications(CompanyRep rep) {
        List<Internship> mine = internshipManager.getInternshipsForRep(rep);
        Internship selection = console.selectInternshipFromList(mine);
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
        int selectionIndex = console.readInt("Select application to update (0 to cancel): ", 0, applications.size());
        if (selectionIndex == 0) {
            return;
        }
        Application target = applications.get(selectionIndex - 1);
        System.out.println("1. Successful");
        System.out.println("2. Unsuccessful");
        int choice = console.readInt("Select new status: ", 1, 2);
        ApplicationStatus status = choice == 1
                ? ApplicationStatus.SUCCESSFUL
                : ApplicationStatus.UNSUCCESSFUL;
        applicationManager.updateStatus(target, status);
        System.out.println("Application status updated to " + status);
    }

    private LocalDate promptValidCloseDate(LocalDate openDate) {
        while (true) {
            LocalDate closeDate = console.readOptionalDate("Close date (yyyy-MM-dd, blank for none): ");
            if (closeDate == null) {
                return null;
            }
            if (closeDate.isBefore(LocalDate.now())) {
                System.out.println("Closing date cannot be in the past.");
                continue;
            }
            if (openDate != null && closeDate.isBefore(openDate)) {
                System.out.println("Closing date cannot be earlier than the opening date.");
                continue;
            }
            return closeDate;
        }
    }
}
