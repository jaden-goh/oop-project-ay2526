package menu;

import boundary.ConsoleHelper;
import control.InternshipManager;
import control.NotificationManager;
import control.ReportGenerator;
import control.UserManager;
import control.WithdrawalManager;
import entity.AccountRequest;
import entity.CareerCenterStaff;
import entity.CompanyRep;
import entity.Internship;
import entity.InternshipLevel;
import entity.InternshipStatus;
import entity.User;
import entity.WithdrawalRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class StaffMenu {
    private final ConsoleHelper console;
    private final InternshipManager internshipManager;
    private final UserManager userManager;
    private final WithdrawalManager withdrawalManager;
    private final NotificationManager notificationManager;
    private final ReportGenerator reportGenerator;
    private final Consumer<User> notificationDisplay;
    private final Consumer<User> passwordChanger;
    private final BiConsumer<String, Boolean> approvalUpdater;

    public StaffMenu(ConsoleHelper console,
                     InternshipManager internshipManager,
                     UserManager userManager,
                     WithdrawalManager withdrawalManager,
                     NotificationManager notificationManager,
                     ReportGenerator reportGenerator,
                     Consumer<User> notificationDisplay,
                     Consumer<User> passwordChanger,
                     BiConsumer<String, Boolean> approvalUpdater) {
        this.console = console;
        this.internshipManager = internshipManager;
        this.userManager = userManager;
        this.withdrawalManager = withdrawalManager;
        this.notificationManager = notificationManager;
        this.reportGenerator = reportGenerator;
        this.notificationDisplay = notificationDisplay;
        this.passwordChanger = passwordChanger;
        this.approvalUpdater = approvalUpdater;
    }

    public void show(CareerCenterStaff staff) {
        boolean exit = false;
        while (!exit) {
            queueStaffWorkloadAlerts(staff);
            notificationDisplay.accept(staff);
            System.out.println("\n=== Career Center Console: " + staff.getName() + " ===");
            System.out.println("1. Review company representative accounts");
            System.out.println("2. Review internship submissions");
            System.out.println("3. Process withdrawal requests");
            System.out.println("4. Generate reports");
            System.out.println("5. Change password");
            System.out.println("6. Back to main menu");
            String choice = console.readLine("Choice: ");
            switch (choice) {
                case "1" -> reviewAccountRequests(staff);
                case "2" -> reviewInternshipSubmissions(staff);
                case "3" -> processWithdrawalRequests(staff);
                case "4" -> showReportsMenu();
                case "5" -> passwordChanger.accept(staff);
                case "6" -> exit = true;
                default -> System.out.println("Unknown option.");
            }
        }
    }

    private void reviewAccountRequests(CareerCenterStaff staff) {
        List<AccountRequest> pending = userManager.getPendingAccounts(1, 100, AccountRequest.STATUS_PENDING);
        if (pending.isEmpty()) {
            System.out.println("No pending company representative requests.");
            return;
        }
        System.out.println("\nPending representative accounts:");
        for (int i = 0; i < pending.size(); i++) {
            AccountRequest request = pending.get(i);
            CompanyRep rep = request.getRep();
            System.out.println((i + 1) + ". " + rep.getName() + " (" + rep.getUserID() + ") - "
                    + rep.getCompanyName());
        }
        int choice = console.readInt("Select a request to process (0 to cancel): ", 0, pending.size());
        if (choice == 0) {
            return;
        }
        AccountRequest request = pending.get(choice - 1);
        if (console.promptYesNo("Approve this account? (y/n): ", true)) {
            staff.approveRepAccount(userManager, request);
            approvalUpdater.accept(request.getRep().getUserID(), true);
            System.out.println("Account approved for " + request.getRep().getUserID());
            notificationManager.notifyRepAccountDecision(request.getRep(), true, null);
            notificationManager.clearNotificationsForUsers(
                    userManager.getCareerCenterStaffMembers(),
                    notification -> notification.getMessage() != null
                            && notification.getMessage().equals(repRegistrationMessage(request.getRep())));
        } else {
            String notes = console.readLine("Reason for rejection: ");
            staff.rejectRepAccount(userManager, request, notes);
            approvalUpdater.accept(request.getRep().getUserID(), false);
            System.out.println("Account rejected.");
            notificationManager.notifyRepAccountDecision(request.getRep(), false, notes);
            notificationManager.clearNotificationsForUsers(
                    userManager.getCareerCenterStaffMembers(),
                    notification -> notification.getMessage() != null
                            && notification.getMessage().equals(repRegistrationMessage(request.getRep())));
        }
    }

    private void reviewInternshipSubmissions(CareerCenterStaff staff) {
        List<Internship> pending = new ArrayList<>();
        for (Internship internship : internshipManager.getInternships()) {
            if (internship.getStatus() == InternshipStatus.PENDING) {
                pending.add(internship);
            }
        }
        if (pending.isEmpty()) {
            System.out.println("No pending internships to review.");
            return;
        }
        System.out.println("\nPending internships:");
        for (int i = 0; i < pending.size(); i++) {
            console.printInternshipRow(i + 1, pending.get(i));
        }
        int choice = console.readInt("Select an internship to review (0 to cancel): ", 0, pending.size());
        if (choice == 0) {
            return;
        }
        Internship target = pending.get(choice - 1);
        if (console.promptYesNo("Approve this internship? (y/n): ", true)) {
            staff.approveInternship(internshipManager, target);
            System.out.println("Internship approved.");
        } else {
            staff.rejectInternship(internshipManager, target);
            System.out.println("Internship rejected.");
        }
        notificationManager.clearNotificationsForUsers(
                userManager.getCareerCenterStaffMembers(),
                notification -> notification.getMessage() != null
                        && notification.getMessage().equals(internshipSubmissionMessage(target)));
    }

    private void processWithdrawalRequests(CareerCenterStaff staff) {
        List<WithdrawalRequest> requests = withdrawalManager.getPendingRequests();
        if (requests.isEmpty()) {
            System.out.println("No withdrawal requests pending.");
            return;
        }
        System.out.println("\nWithdrawal requests:");
        for (int i = 0; i < requests.size(); i++) {
            WithdrawalRequest request = requests.get(i);
            System.out.println((i + 1) + ". " + request.getStudent().getName()
                    + " - " + request.getApplication().getInternship().getTitle()
                    + " | Reason: " + request.getReason());
        }
        int choice = console.readInt("Select a request to process (0 to cancel): ", 0, requests.size());
        if (choice == 0) {
            return;
        }
        WithdrawalRequest target = requests.get(choice - 1);
        boolean approve = console.promptYesNo("Approve this withdrawal? (y/n): ", true);
        staff.processWithdrawal(withdrawalManager, target, approve);
        System.out.println("Withdrawal request processed.");
        notificationManager.clearNotificationsForUsers(
                userManager.getCareerCenterStaffMembers(),
                notification -> notification.getMessage() != null
                        && notification.getMessage().equals(withdrawalRequestMessage(target)));
    }

    private String repRegistrationMessage(CompanyRep rep) {
        if (rep == null) {
            return "";
        }
        return "New company representative registration awaiting approval: "
                + rep.getName() + " (" + rep.getUserID() + ") from "
                + rep.getCompanyName() + ".";
    }

    private String internshipSubmissionMessage(Internship internship) {
        if (internship == null) {
            return "";
        }
        return "New internship submission pending review: "
                + internship.getTitle() + " from " + internship.getCompanyName() + ".";
    }

    private String withdrawalRequestMessage(WithdrawalRequest request) {
        if (request == null || request.getStudent() == null || request.getApplication() == null
                || request.getApplication().getInternship() == null) {
            return "";
        }
        return "Withdrawal request submitted by " + request.getStudent().getName()
                + " for " + request.getApplication().getInternship().getTitle() + ".";
    }

    private void showReportsMenu() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=== Reports ===");
            System.out.println("1. By status");
            System.out.println("2. By preferred major");
            System.out.println("3. By internship level");
            System.out.println("4. Company summary");
            System.out.println("5. Back");
            String choice = console.readLine("Choice: ");
            switch (choice) {
                case "1" -> runStatusReport();
                case "2" -> runMajorReport();
                case "3" -> runLevelReport();
                case "4" -> runCompanyReport();
                case "5" -> exit = true;
                default -> System.out.println("Unknown option.");
            }
        }
    }

    private void runStatusReport() {
        InternshipStatus status = console.promptStatusSelection();
        reportGenerator.generateByStatus(internshipManager.getInternships(), status);
    }

    private void runMajorReport() {
        String major = console.readLine("Preferred major: ");
        if (major.isEmpty()) {
            System.out.println("Major cannot be empty.");
            return;
        }
        reportGenerator.generateByMajor(internshipManager.getInternships(), major);
    }

    private void runLevelReport() {
        InternshipLevel level = console.promptInternshipLevel();
        reportGenerator.generateByLevel(internshipManager.getInternships(), level);
    }

    private void runCompanyReport() {
        String company = console.readLine("Company name: ");
        if (company.isEmpty()) {
            System.out.println("Company cannot be empty.");
            return;
        }
        reportGenerator.generateCompanySummary(internshipManager.getInternships(), company);
    }

    private void queueStaffWorkloadAlerts(CareerCenterStaff staff) {
        if (staff == null) {
            return;
        }
        int pendingInternships = countPendingInternshipSubmissions();
        int pendingWithdrawals = withdrawalManager.getPendingRequests().size();
        notificationManager.notifyStaffWorkloadReminder(staff, pendingInternships, pendingWithdrawals);
    }

    private int countPendingInternshipSubmissions() {
        int count = 0;
        for (Internship internship : internshipManager.getInternships()) {
            if (internship.getStatus() == InternshipStatus.PENDING) {
                count++;
            }
        }
        return count;
    }
}
