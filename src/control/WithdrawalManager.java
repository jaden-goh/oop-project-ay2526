// documented

package control;

import entity.Application;
import entity.CareerCenterStaff;
import entity.Internship;
import entity.InternshipSlot;
import entity.InternshipStatus;
import entity.WithdrawalRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages withdrawal requests for internship applications.
 *
 * <p>Responsibilities include:</p>
 * <ul>
 *     <li>Submitting new withdrawal requests</li>
 *     <li>Processing requests (approve or reject)</li>
 *     <li>Releasing internship slots when withdrawals are approved</li>
 *     <li>Providing access to all pending withdrawal requests</li>
 * </ul>
 */

public class WithdrawalManager {

    /** List of currently pending withdrawal requests. */
    private final List<WithdrawalRequest> requests = new ArrayList<>();

    /**
     * Submits a withdrawal request for the given application.
     *
     * <p>Each application may only have one withdrawal request. Attempting to submit
     * another request for the same application will result in an exception.</p>
     *
     * @param app    the application for which the withdrawal is requested
     * @param reason the reason provided by the student (may be null or blank)
     * @return the created {@link WithdrawalRequest}
     *
     * @throws IllegalArgumentException if the application is null
     * @throws IllegalStateException    if a withdrawal has already been requested for this application
     */
    public WithdrawalRequest submitRequest(Application app, String reason) {
        if (app == null) {
            throw new IllegalArgumentException("Application required");
        }
        if (app.isWithdrawalRequested()) {
            throw new IllegalStateException("Withdrawal already requested.");
        }
        String trimmedReason = reason == null ? "" : reason.trim();
        WithdrawalRequest request = app.requestWithdrawal(trimmedReason);
        requests.add(request);
        return request;
    }

    /**
     * Processes a withdrawal request by either approving or rejecting it.
     *
     * <p>If approved, the corresponding internship slot is released and the
     * internship status is set back to {@link InternshipStatus#APPROVED} and
     * made visible again.</p>
     *
     * @param request the withdrawal request to process
     * @param staff   the staff member processing the request
     * @param approve true to approve, false to reject
     * @return true if processing was successful, false if arguments are invalid
     */
    public boolean processRequest(WithdrawalRequest request, CareerCenterStaff staff, boolean approve) {
        if (request == null || staff == null) {
            return false;
        }
        request.setProcessedBy(staff);
        if (approve) {
            request.approve();
            releaseSlot(request.getApplication());
        } else {
            request.reject();
        }
        requests.remove(request);
        return true;
    }

    /**
     * Returns an unmodifiable view of all currently pending withdrawal requests.
     *
     * @return list of pending {@link WithdrawalRequest} objects
     */
    public List<WithdrawalRequest> getPendingRequests() {
        return Collections.unmodifiableList(requests);
    }

    /**
     * Releases the internship slot associated with the application's student
     * and restores the internship's status to {@link InternshipStatus#APPROVED}
     * and visibility to true.
     *
     * @param application the application whose slot should be released
     */
    private void releaseSlot(Application application) {
        Internship internship = application.getInternship();
        for (InternshipSlot slot : internship.getSlots()) {
            if (slot.getAssignedStudent() == application.getStudent()) {
                slot.release();
                internship.setStatus(InternshipStatus.APPROVED);
                internship.toggleVisibility(true);
                break;
            }
        }
    }
}
