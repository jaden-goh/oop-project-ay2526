// documented
package control;

import entity.Application;
import entity.CareerCenterStaff;
import entity.CompanyRep;
import entity.Internship;
import entity.Notification;
import entity.Student;
import entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Manages in-system notifications for users.
 *
 * <p>This class provides functionality to:</p>
 * <ul>
 *     <li>Send notifications to individual users or groups of users</li>
 *     <li>Notify staff of registration, withdrawal, and internship submission events</li>
 *     <li>Notify students and company representatives of application-related events</li>
 *     <li>Retrieve (consume/peek) notifications from a user's inbox</li>
 *     <li>Clear notifications matching a given condition</li>
 * </ul>
 *
 * <p>Notifications are stored in memory and grouped by user ID.</p>
 */

public class NotificationManager {

    /** Maps each user's ID to their list of notifications. */
    private final Map<String, List<Notification>> inbox = new HashMap<>();

    /**
     * Sends a notification with the given message to a single user.
     *
     * @param user    the target user
     * @param message the notification message content
     */
    public void notifyUser(User user, String message) {
        if (user == null || message == null || message.isBlank()) {
            return;
        }
        inbox.computeIfAbsent(user.getUserID(), key -> new ArrayList<>())
                .add(new Notification(message, LocalDateTime.now()));
    }

    /**
     * Sends the same notification message to a collection of users.
     *
     * @param users   the users to notify
     * @param message the message to send
     */
    public void notifyUsers(Collection<? extends User> users, String message) {
        if (users == null || users.isEmpty()) {
            return;
        }
        for (User user : users) {
            notifyUser(user, message);
        }
    }

    /**
     * Notifies all career center staff that a new company representative
     * registration is pending approval.
     *
     * @param staff       collection of staff to notify
     * @param repName     representative's name
     * @param repId       representative's ID
     * @param companyName the representative's company name
     */
    public void notifyStaffNewRepRegistration(Collection<CareerCenterStaff> staff,
                                              String repName, String repId, String companyName) {
        if (staff == null || staff.isEmpty()) {
            return;
        }
        String message = "New company representative registration awaiting approval: "
                + repName + " (" + repId + ") from " + companyName + ".";
        notifyUsers(staff, message);
    }

    /**
     * Notifies all career center staff that a student has submitted a withdrawal request.
     *
     * @param staff      collection of staff
     * @param student    the student requesting withdrawal
     * @param internship the internship from which they wish to withdraw
     */
    public void notifyStaffWithdrawalRequest(Collection<CareerCenterStaff> staff,
                                             Student student, Internship internship) {
        if (staff == null || staff.isEmpty() || student == null || internship == null) {
            return;
        }
        String message = "Withdrawal request submitted by " + student.getName()
                + " for " + internship.getTitle() + ".";
        notifyUsers(staff, message);
    }

    /**
     * Notifies all career center staff that a new internship has been submitted
     * and is pending review.
     *
     * @param staff      collection of staff
     * @param internship the submitted internship
     */
    public void notifyStaffInternshipSubmission(Collection<CareerCenterStaff> staff,
                                                Internship internship) {
        if (staff == null || staff.isEmpty() || internship == null) {
            return;
        }
        String message = "New internship submission pending review: "
                + internship.getTitle() + " from " + internship.getCompanyName() + ".";
        notifyUsers(staff, message);
    }

    /**
     * Sends workload reminder notifications to a specific staff member based on
     * counts of pending items.
     *
     * @param staff              the staff member
     * @param pendingInternships number of pending internship submissions
     * @param pendingWithdrawals number of pending withdrawal requests
     */
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

    /**
     * Notifies a company representative of the outcome of their account application.
     *
     * <p>Currently only sends a notification if the account was rejected.</p>
     *
     * @param rep      the representative
     * @param approved true if approved, false if rejected
     * @param notes    optional rejection notes
     */
    public void notifyRepAccountDecision(CompanyRep rep, boolean approved, String notes) {
        if (rep == null) {
            return;
        }
        if (approved) {
            return;
        }
        String message = "Your company representative account was rejected.";
        if (notes != null && !notes.isBlank()) {
            message += " Reason: " + notes;
        }
        notifyUser(rep, message);
    }

    /**
     * Removes notifications for given users that match the specified condition.
     * If a user's inbox becomes empty, their entry is removed from the map.
     *
     * @param users     the users whose notifications should be filtered
     * @param condition predicate that returns true for notifications to remove
     */
    public void clearNotificationsForUsers(Collection<? extends User> users,
                                           Predicate<Notification> condition) {
        if (users == null || users.isEmpty() || condition == null) {
            return;
        }
        for (User user : users) {
            if (user == null) {
                continue;
            }
            List<Notification> notifications = inbox.get(user.getUserID());
            if (notifications == null || notifications.isEmpty()) {
                continue;
            }
            notifications.removeIf(condition);
            if (notifications.isEmpty()) {
                inbox.remove(user.getUserID());
            }
        }
    }

    /**
     * Notifies a student that their application was successful and is now
     * awaiting their acceptance.
     *
     * @param application the successful application
     */
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

    /**
     * Notifies a company representative when a new application is received
     * for one of their internships.
     *
     * @param rep        the representative
     * @param student    the applicant
     * @param internship the internship applied for
     */
    public void notifyRepNewApplication(CompanyRep rep, Student student, Internship internship) {
        if (rep == null || student == null || internship == null) {
            return;
        }
        String message = "New application received from " + student.getName()
                + " for " + internship.getTitle() + ".";
        notifyUser(rep, message);
    }

    /**
     * Retrieves and removes all notifications for the given user.
     * The returned list is sorted by timestamp in ascending order.
     *
     * @param user the user whose notifications should be consumed
     * @return unmodifiable sorted list of notifications, or an empty list if none
     */
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

    /**
     * Returns a snapshot of the current notifications for the given user
     * without removing them from the inbox.
     * The returned list is sorted by timestamp in ascending order.
     *
     * @param user the user to check
     * @return unmodifiable sorted list of notifications, or an empty list if none
     */
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

    /**
     * Checks if the given user currently has any notifications.
     *
     * @param user the user to check
     * @return true if notifications exist, false otherwise
     */
    public boolean hasNotifications(User user) {
        if (user == null) {
            return false;
        }
        List<Notification> notifications = inbox.get(user.getUserID());
        return notifications != null && !notifications.isEmpty();
    }
}
