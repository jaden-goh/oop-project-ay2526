// finished with documentation

package entity;

import java.time.LocalDateTime;

/**
 * Represents a request submitted by a {@link CompanyRep} to create an account
 * in the system. The request is reviewed by a {@link CareerCenterStaff}, who can
 * approve or reject it. Each request tracks its submission timestamp, decision
 * status, approver, and decision notes.
 *
 * <p>Statuses include:
 * <ul>
 *     <li>{@link #STATUS_PENDING}</li>
 *     <li>{@link #STATUS_APPROVED}</li>
 *     <li>{@link #STATUS_REJECTED}</li>
 * </ul>
 *
 * The default status is {@code Pending} upon creation.
 */

public class AccountRequest {

    /** Status indicating that the account request is awaiting review. */

    public static final String STATUS_PENDING = "Pending";

    /** Status indicating that the account request has been approved. */
    public static final String STATUS_APPROVED = "Approved";

    /** Status indicating that the account request has been rejected. */
    public static final String STATUS_REJECTED = "Rejected";

    /** The company representative who submitted the account creation request. */
    private final CompanyRep rep;

    /** Timestamp of when the request was submitted. */

    private final LocalDateTime submittedOn = LocalDateTime.now();

    /** Current status of the request. Defaults to {@code Pending}. */
    private String status = STATUS_PENDING;

    /** The career center staff who processed the request, if any. */
    private CareerCenterStaff approver;

    /** Timestamp of when a decision (approve/reject) was made. */
    private LocalDateTime decisionOn;

    /** Optional notes explaining the decision made by the approver. */
    private String decisionNotes;

    /**
     * Creates a new account request for the given company representative.
     *
     * @param rep The representative requesting an account.
     */
    public AccountRequest(CompanyRep rep) {
        this.rep = rep;
    }

    /**
     * Returns the company representative who submitted the request.
     *
     * @return the submitting {@link CompanyRep}
     */
    public CompanyRep getRep() {
        return rep;
    }

    /**
     * Returns the current status of the request.
     *
     * @return one of the status constants: Pending, Approved, or Rejected
     */
    public String getStatus() {
        return status;
    }

    /**
     * Updates the status of the request. Only {@link #STATUS_PENDING},
     * {@link #STATUS_APPROVED}, and {@link #STATUS_REJECTED} are allowed.
     * Also updates the decision timestamp.
     *
     * @param status The new status to assign.
     * @throws IllegalArgumentException if an invalid status is provided
     */
    public void setStatus(String status) {
        if (!STATUS_PENDING.equals(status) && !STATUS_APPROVED.equals(status) && !STATUS_REJECTED.equals(status)) {
            throw new IllegalArgumentException("Unknown status: " + status);
        }
        this.status = status;
        this.decisionOn = LocalDateTime.now();
    }

    /**
     * Returns the staff member who approved or rejected the request.
     *
     * @return the {@link CareerCenterStaff} approver, or null if not processed yet
     */
    public CareerCenterStaff getApprover() {
        return approver;
    }

    /**
     * Assigns the career center staff member who processes this request.
     *
     * @param approver The staff member making the decision.
     */
    public void setApprover(CareerCenterStaff approver) {
        this.approver = approver;
    }

    /**
     * Returns the timestamp when the request was first submitted.
     *
     * @return submission time
     */
    public LocalDateTime getSubmittedOn() {
        return submittedOn;
    }

    /**
     * Returns the time when a decision (approve/reject) was made.
     *
     * @return decision timestamp, or null if still pending
     */
    public LocalDateTime getDecisionOn() {
        return decisionOn;
    }

    /**
     * Returns the notes recorded by the approver explaining their decision.
     *
     * @return decision notes, or null if none
     */
    public String getDecisionNotes() {
        return decisionNotes;
    }

    /**
     * Sets additional notes regarding the approval or rejection decision.
     *
     * @param decisionNotes the notes to record
     */
    public void setDecisionNotes(String decisionNotes) {
        this.decisionNotes = decisionNotes;
    }
}
