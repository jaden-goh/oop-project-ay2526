// documented

package control;

import entity.Application;
import entity.ApplicationStatus;
import entity.CompanyRep;
import entity.Internship;
import entity.InternshipLevel;
import entity.InternshipSlot;
import entity.InternshipStatus;
import entity.Student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all operations related to student internship applications.
 *
 * <p>This class handles:</p>
 * <ul>
 *     <li>Submitting applications</li>
 *     <li>Applying all eligibility and system rules</li>
 *     <li>Updating application statuses</li>
 *     <li>Assigning and releasing internship slots</li>
 *     <li>Sending notifications through {@link NotificationManager}</li>
 * </ul>
 *
 * <p>The manager enforces a maximum of 3 active applications per student.</p>
 */

public class ApplicationManager {

    /** Maximum number of applications a student may have that are pending or successful. */
    private static final int MAX_ACTIVE_APPLICATIONS = 3;

    /** Stores submission log messages (informational only). */
    private final List<String> submissionNotifications = new ArrayList<>();

    /** Stores the reason for the last application rule failure. */
    private String Reason = "";

    /** Handles delivery of notifications to students and company representatives. */
    private NotificationManager notificationManager;

    /**
     * Submits an application for a student to an internship.
     *
     * <p>All eligibility rules are checked via {@link #enforceRules(Student, Internship)}.
     * If any rule fails, an exception is thrown containing the failure reason.</p>
     *
     * @param student     the student applying
     * @param internship  the target internship
     * @return the created {@link Application}
     *
     * @throws IllegalArgumentException if either argument is null
     * @throws IllegalStateException    if application violates a system rule
     */
    public Application submitApplication(Student student, Internship internship) {
        if (student == null || internship == null) {
            throw new IllegalArgumentException("Student and internship are required.");
        }
        if (!enforceRules(student, internship)) {
            throw new IllegalStateException(Reason);
        }
        Application application = new Application(student, internship);
        internship.addApplication(application);
        student.getApplications().add(application);
        notifyRepOfNewApplication(student, internship);
        submissionNotifications.add(LocalDateTime.now() + " :: "
                + student.getName() + " applied for " + internship.getTitle());
        Reason = "";
        return application;
    }

    /**
     * Updates the status of an application.
     *
     * @param application the application to update
     * @param status      new status
     */
    public void updateStatus(Application application, ApplicationStatus status) {
        updateStatus(application, status, false);
    }

    /**
     * Updates the status of an application, optionally confirming an offer.
     *
     * @param application  the target application
     * @param status       new status
     * @param confirmOffer whether the status update is part of final offer acceptance
     */
    public void updateStatus(Application application, ApplicationStatus status, boolean confirmOffer) {
        if (application == null || status == null) {
            return;
        }
        application.setStatus(status);
        if (status == ApplicationStatus.SUCCESSFUL) {
            if (!confirmOffer) {
                notifyStudentOfSuccessfulApplication(application);
            } else {
                assignSlot(application);
            }
        } else if (status == ApplicationStatus.UNSUCCESSFUL) {
            releaseSlot(application);
        }
    }

    /**
     * Applies all system rules to validate whether a student may apply to an internship.
     *
     * <p>Checks include:</p>
     * <ul>
     *     <li>Internship is approved and visible</li>
     *     <li>Major matches</li>
     *     <li>Correct internship level for year of study</li>
     *     <li>Application period is open</li>
     *     <li>Student has fewer than 3 active applications</li>
     *     <li>Student has not already applied for the same internship</li>
     *     <li>Student has not already accepted another placement</li>
     *     <li>Internship still has open slots</li>
     * </ul>
     *
     * @param student     the student
     * @param internship  the internship
     * @return true if all rules pass, false otherwise (reason stored in {@link #Reason})
     */
    public boolean enforceRules(Student student, Internship internship) {
        if (student == null || internship == null) {
            Reason = "Student and internship are required.";
            return false;
        }
        if (internship.getStatus() != InternshipStatus.APPROVED) {
            Reason = "Internship has not been approved yet.";
            return false;
        }
        if (!internship.isVisible()) {
            Reason = "Internship is currently hidden.";
            return false;
        }
        String studentMajor = student.getMajor();
        if (!internship.acceptsMajor(studentMajor)) {
            Reason = "Major does not match the preferred major for this internship.";
            return false;
        }
        InternshipLevel level = internship.getLevel();
        if (student.getYearOfStudy() <= 2 && level != null && level != InternshipLevel.BASIC) {
            Reason = "Lower-year students may only apply for BASIC level internships.";
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate openDate = internship.getOpenDate();
        if (openDate != null && today.isBefore(openDate)) {
            Reason = "Internship is not open for applications yet.";
            return false;
        }
        LocalDate closeDate = internship.getCloseDate();
        if (closeDate != null && today.isAfter(closeDate)) {
            Reason = "Internship is already closed.";
            return false;
        }
        long activeCount = student.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING
                        || app.getStatus() == ApplicationStatus.SUCCESSFUL)
                .count();
        if (activeCount >= MAX_ACTIVE_APPLICATIONS) {
            Reason = "Maximum of " + MAX_ACTIVE_APPLICATIONS + " active applications reached.";
            return false;
        }
        for (Application application : student.getApplications()) {
            if (application.getInternship() == internship
                    && application.getStatus() != ApplicationStatus.UNSUCCESSFUL) {
                Reason = "You have already applied for this internship.";
                return false;
            }
        }
        if (student.hasAcceptedPlacement()) {
            Reason = "You have already accepted a placement.";
            return false;
        }
        if (internship.isFull()) {
            Reason = "Internship slots have been filled.";
            return false;
        }
        Reason = "";
        return true;
    }

    /**
     * Returns the last rule failure message from {@link #enforceRules}.
     *
     * @return the last failure reason
     */
    public String getLastFailureReason() {
        return Reason;
    }

    /**
     * Assigns a notification manager.
     *
     * @param notificationManager notification manager instance
     */
    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    /**
     * Assigns the student to the first available internship slot.
     * If the internship becomes full, remaining applications are marked unsuccessful.
     *
     * @param application the successful application
     */
    private void assignSlot(Application application) {
        Internship internship = application.getInternship();
        for (InternshipSlot slot : internship.getSlots()) {
            if (slot.getAssignedStudent() == null) {
                slot.assignStudent(application.getStudent());
                break;
            }
        }
        if (internship.isFull()) {
            internship.setStatus(InternshipStatus.FILLED);
            markUnassignedApplicationsUnsuccessful(internship);
        }
    }

    /**
     * Releases the slot assigned to the application's student.
     *
     * @param application the application being released
     */
    private void releaseSlot(Application application) {
        Internship internship = application.getInternship();
        for (InternshipSlot slot : internship.getSlots()) {
            if (slot.getAssignedStudent() == application.getStudent()) {
                slot.release();
                internship.setStatus(InternshipStatus.APPROVED);
                break;
            }
        }
    }

    /**
     * Marks all applications for the internship as unsuccessful if the applicant
     * did not receive a slot.
     *
     * @param internship the internship
     */
    private void markUnassignedApplicationsUnsuccessful(Internship internship) {
        if (internship == null) {
            return;
        }
        for (Application other : internship.getApplications()) {
            Student applicant = other.getStudent();
            if (!isStudentAssignedToInternship(internship, applicant)) {
                other.setStatus(ApplicationStatus.UNSUCCESSFUL);
            }
        }
    }

    /**
     * Checks whether the student is assigned to any slot of the internship.
     *
     * @param internship the internship
     * @param student    the student
     * @return true if assigned, false otherwise
     */
    private boolean isStudentAssignedToInternship(Internship internship, Student student) {
        if (internship == null || student == null) {
            return false;
        }
        for (InternshipSlot slot : internship.getSlots()) {
            if (slot.getAssignedStudent() == student) {
                return true;
            }
        }
        return false;
    }

    /**
     * Notifies the student when their application is marked as successful
     * (offer awaiting acceptance).
     *
     * @param application the successful application
     */
    private void notifyStudentOfSuccessfulApplication(Application application) {
        if (notificationManager == null) {
            return;
        }
        notificationManager.notifyStudentOfferAwaitingAcceptance(application);
    }

    /**
     * Notifies the company representative when a new application is received.
     *
     * @param student     the applicant
     * @param internship  the target internship
     */
    private void notifyRepOfNewApplication(Student student, Internship internship) {
        if (notificationManager == null || student == null || internship == null) {
            return;
        }
        CompanyRep rep = internship.getRepInCharge();
        if (rep == null) {
            return;
        }
        notificationManager.notifyRepNewApplication(rep, student, internship);
    }
}
