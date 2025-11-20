// documented

package entity;

import control.ApplicationManager;
import control.WithdrawalManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student user in the internship management system.
 *
 * <p>A student can:</p>
 * <ul>
 *     <li>Apply for eligible internships (up to 3 active applications)</li>
 *     <li>Withdraw applications (subject to staff approval)</li>
 *     <li>Accept exactly one successful placement</li>
 * </ul>
 *
 * <p>The student tracks their applications and maintains a reference
 * to the accepted placement when confirmed.</p>
 */
public class Student extends User {

    /** The student's academic year (1–4). */
    private final int yearOfStudy;

    /** The student's declared major. */
    private final String major;

    /** All internship applications submitted by the student. */
    private final List<Application> applications = new ArrayList<>();

    /** The student's accepted internship placement, if any. */
    private Application acceptedPlacement;

    /**
     * Creates a new student.
     *
     * @param userID       student ID with NTU format (e.g., U1234567A)
     * @param name         student name
     * @param password     student password
     * @param yearOfStudy  academic year of the student
     * @param major        major program of the student
     */
    public Student(String userID, String name, String password, int yearOfStudy, String major) {
        super(userID, name, password);
        this.yearOfStudy = yearOfStudy;
        this.major = major;
    }

    /**
     * Returns the student's academic year.
     *
     * @return year of study
     */
    public int getYearOfStudy() {
        return yearOfStudy;
    }

    /**
     * Returns the student's major.
     *
     * @return student major
     */
    public String getMajor() {
        return major;
    }

    /**
     * Returns the list of applications submitted by this student.
     *
     * @return list of applications
     */
    public List<Application> getApplications() {
        return applications;
    }

    /**
     * Attempts to apply for the given internship using the provided {@link ApplicationManager}.
     *
     * @param internship the internship to apply for
     * @param manager    the application manager handling the submission
     * @return true if submission succeeds, false if rejected or invalid
     *
     * @throws IllegalArgumentException if manager is null
     */
    public boolean apply(Internship internship, ApplicationManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("Application manager required.");
        }
        try {
            return manager.submitApplication(this, internship) != null;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Submits a withdrawal request for the specified application.
     *
     * @param application the application to withdraw
     * @param manager     the withdrawal manager processing the request
     * @param reason      justification for withdrawal
     * @return the created {@link WithdrawalRequest}
     *
     * @throws IllegalArgumentException if manager is null,
     *                                  or application does not belong to this student
     */
    public WithdrawalRequest withdraw(Application application, WithdrawalManager manager, String reason) {
        if (manager == null) {
            throw new IllegalArgumentException("Withdrawal manager required.");
        }
        if (application == null || !applications.contains(application)) {
            throw new IllegalArgumentException("Application does not belong to student.");
        }
        return manager.submitRequest(application, reason);
    }

    /**
     * Accepts a successful internship placement.
     *
     * <p>Once accepted:</p>
     * <ul>
     *     <li>This application becomes the student's official placement</li>
     *     <li>All other active applications are marked UNSUCCESSFUL</li>
     * </ul>
     *
     * @param application the successful application to accept
     * @param manager     the application manager updating statuses
     *
     * @throws IllegalArgumentException if application is not owned by the student,
     *                                  or if manager is null
     */
    public void acceptPlacement(Application application, ApplicationManager manager) {
        if (application == null || !applications.contains(application)) {
            throw new IllegalArgumentException("Application does not belong to student.");
        }
        if (manager == null) {
            throw new IllegalArgumentException("Application manager required.");
        }
        acceptedPlacement = application;
        for (Application other : applications) {
            if (other != application && (other.getStatus() == ApplicationStatus.PENDING || other.getStatus() == ApplicationStatus.SUCCESSFUL)) {
                    manager.updateStatus(other, ApplicationStatus.UNSUCCESSFUL);
                }
        }
        manager.updateStatus(application, ApplicationStatus.SUCCESSFUL, true);
    }

    /**
     * Returns whether the student has already accepted a placement.
     *
     * @return true if placement has been accepted
     */
    public boolean hasAcceptedPlacement() {
        return acceptedPlacement != null;
    }

    /**
     * Returns the student's accepted internship placement.
     *
     * @return the accepted application, or null if none
     */
    public Application getAcceptedPlacement() {
        return acceptedPlacement;
    }

}
