import java.util.Date;

public class Application {
    private String applicationID;
    private String status;
    private Date dateApplied;
    private boolean accepted;
    private boolean withdrawn;
    private Student student;
    private Internship internship;

    public Application() {}

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
    public void updateStatus(String newStatus) { }
    public void markAccepted() { }
    public void requestWithdrawal() { }
    public boolean isPending() { return false; }
}

