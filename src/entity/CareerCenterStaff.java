// done with documentation

package entity;

import control.InternshipManager;
import control.UserManager;
import control.WithdrawalManager;

/**
 * Represents a Career Center Staff user in the system.
 * Career Center Staff are responsible for:
 * <ul>
 *     <li>Approving or rejecting internship opportunities</li>
 *     <li>Approving or rejecting company representative account requests</li>
 *     <li>Processing student withdrawal requests</li>
 * </ul>
 *
 * This class extends {@link User} and adds staff-related attributes and operations.
 */

public class CareerCenterStaff extends User {

    /** The department this staff member belongs to. */
    private String staffDepartment;

    /**
     * Creates a new Career Center Staff user.
     *
     * @param userID          the staff member's login ID
     * @param name            the staff member's name
     * @param password        the default or assigned password
     * @param staffDepartment the department this staff member is part of
     */
    public CareerCenterStaff(String userID, String name, String password, String staffDepartment) {
        super(userID, name, password);
        this.staffDepartment = staffDepartment;
    }

    /**
     * Returns the department this staff member belongs to.
     *
     * @return staff department name
     */
    public String getStaffDepartment() {
        return staffDepartment;
    }

    /**
     * Updates the department this staff member belongs to.
     *
     * @param staffDepartment new department name
     */
    public void setStaffDepartment(String staffDepartment) {
        this.staffDepartment = staffDepartment;
    }

    /**
     * Approves an internship opportunity by delegating the action to the
     * {@link InternshipManager}.
     *
     * @param manager    the internship manager handling approvals
     * @param internship the internship to approve
     */
    public void approveInternship(InternshipManager manager, Internship internship) {
        if (manager == null || internship == null) {
            return;
        }
        manager.approveInternship(internship);
    }

    /**
     * Rejects an internship opportunity by delegating to the {@link InternshipManager}.
     *
     * @param manager    the internship manager handling rejections
     * @param internship the internship to reject
     */
    public void rejectInternship(InternshipManager manager, Internship internship) {
        if (manager == null || internship == null) {
            return;
        }
        manager.rejectInternship(internship);
    }

    /**
     * Approves a company representative account request.
     *
     * @param manager the user manager responsible for account approval
     * @param request the account request being approved
     */
    public void approveRepAccount(UserManager manager, AccountRequest request) {
        if (manager == null || request == null || request.getRep() == null) {
            return;
        }
        manager.approveRepresentative(request.getRep().getUserID(), this);
    }

    /**
     * Rejects a company representative account request, optionally including notes.
     *
     * @param manager the user manager responsible for handling company rep accounts
     * @param request the account request to reject
     * @param notes   rejection notes explaining the decision
     */
    public void rejectRepAccount(UserManager manager, AccountRequest request, String notes) {
        if (manager == null || request == null || request.getRep() == null) {
            return;
        }
        manager.rejectRepresentative(request.getRep().getUserID(), this, notes);
    }

    /**
     * Processes a withdrawal request submitted by a student.
     *
     * @param manager the withdrawal manager handling withdrawal logic
     * @param request the withdrawal request to process
     * @param approve {@code true} to approve the withdrawal, {@code false} to reject it
     */
    public void processWithdrawal(WithdrawalManager manager, WithdrawalRequest request, boolean approve) {
        if (manager == null || request == null) {
            return;
        }
        manager.processRequest(request, this, approve);
    }
}
