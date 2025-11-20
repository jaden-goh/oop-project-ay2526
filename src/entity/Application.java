// documentation finished

package entity;

import java.util.Date;

/**
 * Represents a student's application for a specific internship.
 * Each application is associated with one {@link Student} and one {@link Internship},
 * and tracks its submission timestamp, current status, and any withdrawal request
 * made by the student.
 *
 * <p>The default application status is {@link ApplicationStatus#PENDING}.</p>
 */

public class Application {

    /** The student who submitted this application. */
    private final Student student;

    /** The internship being applied for. */
    private final Internship internship;

    /** The current status of this application. */
    private ApplicationStatus status = ApplicationStatus.PENDING;

    /** Timestamp noting when the application was created. */
    private final Date timestamp = new Date();

    /** Optional withdrawal request associated with this application. */
    private WithdrawalRequest withdrawalRequest;

    /**
     * Creates a new application for the given student and internship.
     *
     * @param student    the student submitting the application
     * @param internship the internship being applied to
     */
    public Application(Student student, Internship internship) {
        this.student = student;
        this.internship = internship;
    }

    /**
     * Returns the student who submitted this application.
     *
     * @return the associated {@link Student}
     */
    public Student getStudent() {
        return student;
    }

    /**
     * Returns the internship this application is for.
     *
     * @return the associated {@link Internship}
     */
    public Internship getInternship() {
        return internship;
    }

    /**
     * Returns the current status of the application.
     *
     * @return the application status
     */
    public ApplicationStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the application.
     *
     * @param status the new application status
     */
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    /**
     * Returns the timestamp of when the application was submitted.
     *
     * @return submission timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Checks whether a withdrawal request has already been submitted.
     *
     * @return true if a withdrawal request exists, false otherwise
     */
    public boolean isWithdrawalRequested() {
        return withdrawalRequest != null;
    }

    /**
     * Marks this application as successful.
     */
    public void markSuccessful() {
        this.status = ApplicationStatus.SUCCESSFUL;
    }

    /**
     * Marks this application as unsuccessful.
     */
    public void markUnsuccessful() {
        this.status = ApplicationStatus.UNSUCCESSFUL;
    }

    /**
     * Returns the withdrawal request associated with this application, if any.
     *
     * @return the {@link WithdrawalRequest}, or null if none exists
     */
    public WithdrawalRequest getWithdrawalRequest() {
        return withdrawalRequest;
    }

    /**
     * Submits a withdrawal request for this application.
     *
     * @param reason the student's reason for withdrawal
     * @return the created {@link WithdrawalRequest}
     * @throws IllegalStateException if a withdrawal request has already been submitted
     */
    public WithdrawalRequest requestWithdrawal(String reason) {
        if (withdrawalRequest != null) {
            throw new IllegalStateException("Withdrawal already requested.");
        }
        withdrawalRequest = new WithdrawalRequest(this, student, reason);
        return withdrawalRequest;
    }
}
