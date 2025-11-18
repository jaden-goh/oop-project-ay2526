import java.util.Date;

public class Application {
    private final Student student;
    private final Internship internship;
    private ApplicationStatus status = ApplicationStatus.PENDING;
    private final Date timestamp = new Date();
    private WithdrawalRequest withdrawalRequest;

    public Application(Student student, Internship internship) {
        this.student = student;
        this.internship = internship;
    }

    public Student getStudent() {
        return student;
    }

    public Internship getInternship() {
        return internship;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isWithdrawalRequested() {
        return withdrawalRequest != null;
    }

    public void markSuccessful() {
        this.status = ApplicationStatus.SUCCESSFUL;
    }

    public void markUnsuccessful() {
        this.status = ApplicationStatus.UNSUCCESSFUL;
    }

    public WithdrawalRequest getWithdrawalRequest() {
        return withdrawalRequest;
    }

    public WithdrawalRequest requestWithdrawal(String reason) {
        if (withdrawalRequest != null) {
            throw new IllegalStateException("Withdrawal already requested.");
        }
        withdrawalRequest = new WithdrawalRequest(this, student, reason);
        return withdrawalRequest;
    }
}
