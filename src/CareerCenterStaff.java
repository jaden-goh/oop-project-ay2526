public class CareerCenterStaff extends User {
    private String staffDepartment;

    public CareerCenterStaff(String userID, String name, String password, String staffDepartment) {
        super(userID, name, password);
        this.staffDepartment = staffDepartment;
    }

    public String getStaffDepartment() {
        return staffDepartment;
    }

    public void setStaffDepartment(String staffDepartment) {
        this.staffDepartment = staffDepartment;
    }

    public void approveInternship(InternshipManager manager, Internship internship) {
        if (manager == null || internship == null) {
            return;
        }
        manager.approveInternship(internship);
    }

    public void rejectInternship(InternshipManager manager, Internship internship) {
        if (manager == null || internship == null) {
            return;
        }
        manager.rejectInternship(internship);
    }

    public void approveRepAccount(UserManager manager, AccountRequest request) {
        if (manager == null || request == null || request.getRep() == null) {
            return;
        }
        manager.approveRepresentative(request.getRep().getUserID(), this);
    }

    public void rejectRepAccount(UserManager manager, AccountRequest request, String notes) {
        if (manager == null || request == null || request.getRep() == null) {
            return;
        }
        manager.rejectRepresentative(request.getRep().getUserID(), this, notes);
    }

    public void processWithdrawal(WithdrawalManager manager, WithdrawalRequest request, boolean approve) {
        if (manager == null || request == null) {
            return;
        }
        manager.processRequest(request, this, approve);
    }
}
