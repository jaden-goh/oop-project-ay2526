package entity;
import java.util.Date;
import java.util.Objects;

public class Application {
    private String applicationID;
    private String status = "Pending";  // default pending
    private Date dateApplied;
    private boolean accepted;
    private boolean withdrawn;
    private Student student;
    private Internship internship;

    //public Application() {}

    // constructor class
    public Application(String applicationID, Student student, Internship internship, Date dateApplied) {
        this.applicationID = Objects.requireNonNull(applicationID, "Application ID cannot be null");
        this.student = Objects.requireNonNull(student, "Student cannot be null");
        this.internship = Objects.requireNonNull(internship, "Internship cannot be null");
        this.dateApplied = Objects.requireNonNull(dateApplied, "Date applied cannot be null");
        this.status = "Pending";
        this.accepted = false;
        this.withdrawn = false;
    }


    // Getters and Setters
    public String getApplicationID() { return applicationID; }
    public void setApplicationID(String applicationID) { this.applicationID = applicationID; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getDateApplied() { return dateApplied; }
    public void setDateApplied(Date dateApplied) { this.dateApplied = dateApplied; }

    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }

    public boolean isWithdrawn() { return withdrawn; }
    public void setWithdrawn(boolean withdrawn) { this.withdrawn = withdrawn; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Internship getInternship() { return internship; }
    public void setInternship(Internship internship) { this.internship = internship; }

    // functions

    // updates status + error handling
    public void updateStatus(String newStatus) {
        if (newStatus == null || newStatus.isBlank()) return;
        this.status = newStatus;
    }

    // Accepts only if status = accepted
    public void markAccepted() {
        this.accepted = true;
    }

    public boolean requestWithdrawal() {
        if (!withdrawn){
            withdrawn = true;
            return true;
        }

        return false;
    }
    public boolean isPending() {
        return "Pending".equalsIgnoreCase(status);
    }
}

