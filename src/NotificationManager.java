import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationManager {
    private final Map<String, List<Notification>> inbox = new HashMap<>();

    public void notifyUser(User user, String message) {
        if (user == null || message == null || message.isBlank()) {
            return;
        }
        inbox.computeIfAbsent(user.getUserID(), key -> new ArrayList<>())
                .add(new Notification(message, LocalDateTime.now()));
    }

    public void notifyUsers(Collection<? extends User> users, String message) {
        if (users == null || users.isEmpty()) {
            return;
        }
        for (User user : users) {
            notifyUser(user, message);
        }
    }

    public void notifyStaffNewRepRegistration(Collection<CareerCenterStaff> staff,
                                              String repName, String repId, String companyName) {
        if (staff == null || staff.isEmpty()) {
            return;
        }
        String message = "New company representative registration awaiting approval: "
                + repName + " (" + repId + ") from " + companyName + ".";
        notifyUsers(staff, message);
    }

    public void notifyStaffWithdrawalRequest(Collection<CareerCenterStaff> staff,
                                             Student student, Internship internship) {
        if (staff == null || staff.isEmpty() || student == null || internship == null) {
            return;
        }
        String message = "Withdrawal request submitted by " + student.getName()
                + " for " + internship.getTitle() + ".";
        notifyUsers(staff, message);
    }

    public void notifyStaffInternshipSubmission(Collection<CareerCenterStaff> staff,
                                                Internship internship) {
        if (staff == null || staff.isEmpty() || internship == null) {
            return;
        }
        String message = "New internship submission pending review: "
                + internship.getTitle() + " from " + internship.getCompanyName() + ".";
        notifyUsers(staff, message);
    }

    public void notifyStaffWorkloadReminder(CareerCenterStaff staff,
                                            int pendingInternships, int pendingWithdrawals) {
        if (staff == null) {
            return;
        }
        if (pendingInternships > 0) {
            notifyUser(staff, pendingInternships + " internship submission(s) awaiting review.");
        }
        if (pendingWithdrawals > 0) {
            notifyUser(staff, pendingWithdrawals + " withdrawal request(s) awaiting action.");
        }
    }

    public void notifyRepAccountDecision(CompanyRep rep, boolean approved, String notes) {
        if (rep == null) {
            return;
        }
        String message;
        if (approved) {
            message = "Your company representative account has been approved. You may now log in.";
        } else {
            message = "Your company representative account was rejected.";
            if (notes != null && !notes.isBlank()) {
                message += " Reason: " + notes;
            }
        }
        notifyUser(rep, message);
    }

    public void notifyStudentOfferAwaitingAcceptance(Application application) {
        if (application == null) {
            return;
        }
        Student student = application.getStudent();
        Internship internship = application.getInternship();
        if (student == null || internship == null) {
            return;
        }
        String message = "Application for " + internship.getTitle()
                + " at " + internship.getCompanyName() + " is awaiting your acceptance.";
        notifyUser(student, message);
    }

    public List<Notification> consumeNotifications(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        List<Notification> notifications = inbox.remove(user.getUserID());
        if (notifications == null || notifications.isEmpty()) {
            return Collections.emptyList();
        }
        notifications.sort(Comparator.comparing(Notification::getTimestamp));
        return Collections.unmodifiableList(notifications);
    }

    public List<Notification> peekNotifications(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        List<Notification> notifications = inbox.get(user.getUserID());
        if (notifications == null || notifications.isEmpty()) {
            return Collections.emptyList();
        }
        notifications.sort(Comparator.comparing(Notification::getTimestamp));
        return Collections.unmodifiableList(new ArrayList<>(notifications));
    }

    public boolean hasNotifications(User user) {
        if (user == null) {
            return false;
        }
        List<Notification> notifications = inbox.get(user.getUserID());
        return notifications != null && !notifications.isEmpty();
    }
}
