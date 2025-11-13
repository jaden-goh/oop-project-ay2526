package entity;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import entity.ApplicationStatus;

public class Application {
    private String applicationID;
    private ApplicationStatus status = ApplicationStatus.PENDING;  // default pending
    private LocalDate dateApplied;
    private boolean accepted;
    private boolean withdrawalRequested;
    private boolean withdrawn;
    private Student student;
    private Internship internship;

    //public Application() {}

    // constructor class
    public Application(String applicationID, Student student, Internship internship, LocalDate dateApplied) {
        this.applicationID = Objects.requireNonNull(applicationID, "Application ID cannot be null");
        this.student = Objects.requireNonNull(student, "Student cannot be null");
        this.internship = Objects.requireNonNull(internship, "Internship cannot be null");
        this.dateApplied = Objects.requireNonNull(dateApplied, "Date applied cannot be null");
        this.accepted = false;
        this.withdrawn = false;
    }


    // Getters and Setters
    public String getApplicationID() { return applicationID; }
    public void setApplicationID(String applicationID) { this.applicationID = applicationID; }

    public ApplicationStatus getStatus() { return status; }

    public void setStatus(ApplicationStatus status) {
        if (status != null) { this.status = status; }
    }

    public LocalDate getDateApplied() { return dateApplied; }
    public void setDateApplied(LocalDate dateApplied) { this.dateApplied = dateApplied; }

    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }

    public boolean isWithdrawalRequested() { return withdrawalRequested; }

    public boolean isWithdrawn() { return withdrawn; }
    public void setWithdrawn(boolean withdrawn) { this.withdrawn = withdrawn; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Internship getInternship() { return internship; }
    public void setInternship(Internship internship) { this.internship = internship; }

    // functions

    // updates status + error handling
    public void updateStatus(ApplicationStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
        }
    }

    // Accepts only if status = accepted
    public void markAccepted() {
        this.accepted = true;
        this.status = ApplicationStatus.SUCCESSFUL;
    }

    public void markRejected() {
        this.accepted = false;
        this.status = ApplicationStatus.UNSUCCESSFUL;
    }

    public boolean requestWithdrawal() {
        if (withdrawalRequested || withdrawn) return false;
        withdrawalRequested = true;
        return true;
    }

    public boolean approveWithdrawal() {
        if (withdrawalRequested || withdrawn) return false;
        withdrawn = true;
        withdrawalRequested = false;
        return true;
    }

    public boolean rejectWithdrawal() {
        if (!withdrawalRequested) return false;
        withdrawalRequested = false;
        return true;
    }


    public boolean isPending() {
        return status == ApplicationStatus.PENDING;
    }
}