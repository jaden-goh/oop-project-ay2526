// documented
package entity;

import java.time.LocalDateTime;

/**
 * Represents a student's request to withdraw from an internship application.
 *
 * <p>A withdrawal request is created by a {@link Student} and must be reviewed
 * and processed by a {@link CareerCenterStaff}. The request contains the reason
 * for withdrawal, timestamps for request and processing, and the outcome status.</p>
 *
 * <p>Possible statuses are defined in {@link WithdrawalStatus}.</p>
 */

public class WithdrawalRequest {

    /** The application the student wishes to withdraw from. */
    private final Application application;

    /** The student submitting the withdrawal request. */
    private final Student student;

    /** Reason provided by the student for withdrawal. */
    private final String reason;

    /** Timestamp when the withdrawal request was created. */
    private final LocalDateTime requestedOn = LocalDateTime.now();

    /** Current status of the withdrawal request (defaults to PENDING). */
    private WithdrawalStatus status = WithdrawalStatus.PENDING;

    /** Career center staff member who processed the request. */
    private CareerCenterStaff processedBy;

    /** Timestamp when the withdrawal request was processed. */
    private LocalDateTime processedOn;

    /**
     * Creates a new withdrawal request for the given application and student.
     *
     * @param application the application being withdrawn
     * @param student     the student submitting the request
     * @param reason      justification for withdrawal
     */
    public WithdrawalRequest(Application application, Student student, String reason) {
        this.application = application;
        this.student = student;
        this.reason = reason;
    }

    /**
     * Returns the application associated with this withdrawal request.
     *
     * @return the application
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Returns the student who submitted this withdrawal request.
     *
     * @return the student
     */
    public Student getStudent() {
        return student;
    }

    /**
     * Returns the withdrawal reason provided by the student.
     *
     * @return the reason text
     */
    public String getReason() {
        return reason;
    }

    /**
     * Returns the timestamp when the withdrawal request was created.
     *
     * @return timestamp of creation
     */
    public LocalDateTime getRequestedOn() {
        return requestedOn;
    }

    /**
     * Returns the current status of the withdrawal request.
     *
     * @return request status
     */
    public WithdrawalStatus getStatus() {
        return status;
    }

    /**
     * Returns the staff member who processed the request.
     *
     * @return processing staff, or null if not processed
     */
    public CareerCenterStaff getProcessedBy() {
        return processedBy;
    }

    /**
     * Returns the timestamp when the request was processed.
     *
     * @return processing timestamp, or null if unprocessed
     */
    public LocalDateTime getProcessedOn() {
        return processedOn;
    }

    /**
     * Marks the withdrawal request as approved.
     *
     * <p>This will:</p>
     * <ul>
     *     <li>Transition the request status to {@link WithdrawalStatus#WITHDRAWN}</li>
     *     <li>Set the associated application to {@link ApplicationStatus#WITHDRAWN}</li>
     * </ul>
     */
    public void approve() {
        transition(WithdrawalStatus.WITHDRAWN);
        application.setStatus(ApplicationStatus.WITHDRAWN);
    }

    /**
     * Marks the withdrawal request as rejected.
     */
    public void reject() {
        transition(WithdrawalStatus.REJECTED);
    }

    /**
     * Performs a status transition and timestamps the processing.
     *
     * @param targetStatus the new {@link WithdrawalStatus}
     */
    private void transition(WithdrawalStatus targetStatus) {
        this.status = targetStatus;
        this.processedOn = LocalDateTime.now();
    }

    /**
     * Sets the staff member who processed this request.
     *
     * @param processedBy the processing career center staff
     */
    public void setProcessedBy(CareerCenterStaff processedBy) {
        this.processedBy = processedBy;
    }
}
