import java.time.LocalDateTime;

public class WithdrawalRequest {
    private final Application application;
    private final Student student;
    private final String reason;
    private final LocalDateTime requestedOn = LocalDateTime.now();
    private WithdrawalStatus status = WithdrawalStatus.PENDING;
    private CareerCenterStaff processedBy;
    private LocalDateTime processedOn;

    public WithdrawalRequest(Application application, Student student, String reason) {
        this.application = application;
        this.student = student;
        this.reason = reason;
    }

    public Application getApplication() {
        return application;
    }

    public Student getStudent() {
        return student;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getRequestedOn() {
        return requestedOn;
    }

    public WithdrawalStatus getStatus() {
        return status;
    }

    public CareerCenterStaff getProcessedBy() {
        return processedBy;
    }

    public LocalDateTime getProcessedOn() {
        return processedOn;
    }

    public void approve() {
        transition(WithdrawalStatus.WITHDRAWN);
        application.setStatus(ApplicationStatus.WITHDRAWN);
    }

    public void reject() {
        transition(WithdrawalStatus.REJECTED);
    }

    private void transition(WithdrawalStatus targetStatus) {
        this.status = targetStatus;
        this.processedOn = LocalDateTime.now();
    }

    public void setProcessedBy(CareerCenterStaff processedBy) {
        this.processedBy = processedBy;
    }
}
