package menu;

import boundary.ConsoleHelper;
import boundary.InternshipBrowser;
import control.ApplicationManager;
import control.InternshipManager;
import control.NotificationManager;
import control.UserManager;
import control.WithdrawalManager;
import entity.Application;
import entity.ApplicationStatus;
import entity.Internship;
import entity.InternshipLevel;
import entity.InternshipStatus;
import entity.Student;
import entity.User;
import entity.WithdrawalRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StudentMenu {
    private final ConsoleHelper console;
    private final InternshipBrowser internshipBrowser;
    private final ApplicationManager applicationManager;
    private final InternshipManager internshipManager;
    private final NotificationManager notificationManager;
    private final WithdrawalManager withdrawalManager;
    private final UserManager userManager;
    private final Consumer<User> notificationDisplay;
    private final Consumer<User> passwordChanger;

    public StudentMenu(ConsoleHelper console,
                       InternshipBrowser internshipBrowser,
                       ApplicationManager applicationManager,
                       InternshipManager internshipManager,
                       NotificationManager notificationManager,
                       WithdrawalManager withdrawalManager,
                       UserManager userManager,
                       Consumer<User> notificationDisplay,
                       Consumer<User> passwordChanger) {
        this.console = console;
        this.internshipBrowser = internshipBrowser;
        this.applicationManager = applicationManager;
        this.internshipManager = internshipManager;
        this.notificationManager = notificationManager;
        this.withdrawalManager = withdrawalManager;
        this.userManager = userManager;
        this.notificationDisplay = notificationDisplay;
        this.passwordChanger = passwordChanger;
    }

    public void show(Student student) {
        boolean exit = false;
        while (!exit) {
            notificationDisplay.accept(student);
            System.out.println("\n=== Student Portal: " + student.getName() + " ===");
            System.out.println("1. Browse internships");
            System.out.println("2. Apply to an internship");
            System.out.println("3. View my applications");
            System.out.println("4. Request withdrawal");
            System.out.println("5. Accept an offer");
            System.out.println("6. Change password");
            System.out.println("7. Back to main menu");
            String choice = console.readLine("Choice: ");
            switch (choice) {
                case "1" -> displayInternshipCatalog(student);
                case "2" -> handleStudentApplication(student);
                case "3" -> showStudentApplications(student);
                case "4" -> handleStudentWithdrawal(student);
                case "5" -> handleAcceptOffer(student);
                case "6" -> passwordChanger.accept(student);
                case "7" -> exit = true;
                default -> System.out.println("Unknown option.");
            }
        }
    }

    private void displayInternshipCatalog(Student student) {
        List<Internship> internships = internshipBrowser.fetchFilteredInternships(student, null);
        if (internships.isEmpty()) {
            System.out.println("No internships available yet.");
            return;
        }
        System.out.println("\nAvailable internships:");
        for (int i = 0; i < internships.size(); i++) {
            console.printInternshipRow(i + 1, internships.get(i));
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
        Internship selection = console.selectInternshipFromList(available);
        if (selection == null) {
            return;
        }
        if (student.apply(selection, applicationManager)) {
            System.out.println("Application submitted for " + selection.getTitle());
        } else {
            String failure = applicationManager.getLastFailureReason();
            if (failure != null && !failure.isBlank()) {
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
        Application target = console.selectApplicationFromList(withdrawable, "Select an application to withdraw (0 to cancel): ");
        if (target == null) {
            return;
        }
        String reason = console.readLine("Reason for withdrawal: ");
        try {
            WithdrawalRequest request = student.withdraw(target, withdrawalManager, reason);
            System.out.println("Withdrawal requested. Reference: " + request.getRequestedOn());
            notificationManager.notifyStaffWithdrawalRequest(
                    userManager.getCareerCenterStaffMembers(), student, target.getInternship());
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
        Application target = console.selectApplicationFromList(offers, "Select an application to accept (0 to cancel): ");
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

    private List<Internship> getInternshipsOpenToStudents(Student student) {
        List<Internship> available = new ArrayList<>();
        String studentMajor = student.getMajor();
        int studentYear = student.getYearOfStudy();
        for (Internship internship : internshipManager.getInternships()) {
            if (internship.getStatus() != InternshipStatus.APPROVED
                    || !internship.isVisible() || internship.isFull()) {
                continue;
            }
            if (!internship.acceptsMajor(studentMajor)) {
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
}
