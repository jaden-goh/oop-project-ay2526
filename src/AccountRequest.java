import java.time.LocalDateTime;

public class AccountRequest {
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_APPROVED = "Approved";
    public static final String STATUS_REJECTED = "Rejected";

    private final CompanyRep rep;
    private final LocalDateTime submittedOn = LocalDateTime.now();
    private String status = STATUS_PENDING;
    private CareerCenterStaff approver;
    private LocalDateTime decisionOn;
    private String decisionNotes;

    public AccountRequest(CompanyRep rep) {
        this.rep = rep;
    }

    public CompanyRep getRep() {
        return rep;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (!STATUS_PENDING.equals(status) && !STATUS_APPROVED.equals(status) && !STATUS_REJECTED.equals(status)) {
            throw new IllegalArgumentException("Unknown status: " + status);
        }
        this.status = status;
        this.decisionOn = LocalDateTime.now();
    }

    public CareerCenterStaff getApprover() {
        return approver;
    }

    public void setApprover(CareerCenterStaff approver) {
        this.approver = approver;
    }

    public LocalDateTime getSubmittedOn() {
        return submittedOn;
    }

    public LocalDateTime getDecisionOn() {
        return decisionOn;
    }

    public String getDecisionNotes() {
        return decisionNotes;
    }

    public void setDecisionNotes(String decisionNotes) {
        this.decisionNotes = decisionNotes;
    }
}
