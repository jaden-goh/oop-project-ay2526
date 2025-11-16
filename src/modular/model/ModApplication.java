package modular.model;

import java.time.LocalDate;

public class ModApplication {
    private final String id;
    private final ModStudent student;
    private final ModInternship internship;
    private final LocalDate dateApplied;
    private ModApplicationStatus status = ModApplicationStatus.PENDING;
    private boolean accepted;
    private boolean withdrawn;

    public ModApplication(String id, ModStudent student, ModInternship internship, LocalDate dateApplied) {
        this.id = id;
        this.student = student;
        this.internship = internship;
        this.dateApplied = dateApplied;
    }

    public String getId() { return id; }
    public ModStudent getStudent() { return student; }
    public ModInternship getInternship() { return internship; }
    public LocalDate getDateApplied() { return dateApplied; }

    public ModApplicationStatus getStatus() { return status; }
    public void setStatus(ModApplicationStatus status) { this.status = status; }

    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }

    public boolean isWithdrawn() { return withdrawn; }
    public void setWithdrawn(boolean withdrawn) { this.withdrawn = withdrawn; }
}
