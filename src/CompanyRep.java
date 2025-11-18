import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompanyRep extends User {
    private static final int MAX_INTERNSHIPS = 5;
    private static final int MAX_SLOTS = 10;

    private String companyName;
    private String department;
    private String position;
    private final List<Internship> internships = new ArrayList<>();
    private boolean approved;

    public CompanyRep(String userID, String name, String password,
                      String companyName, String department, String position,
                      boolean approved) {
        super(userID, name, password);
        this.companyName = companyName;
        this.department = department;
        this.position = position;
        this.approved = approved;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public Internship createInternship(InternshipManager manager, String title, String description,
                                       InternshipLevel level, String preferredMajor,
                                       LocalDate openDate, LocalDate closeDate,
                                       int slotCount) {
        ensureApproved();
        if (manager == null) {
            throw new IllegalArgumentException("Internship manager required.");
        }
        if (internships.size() >= MAX_INTERNSHIPS) {
            throw new IllegalStateException("Maximum number of internships (" + MAX_INTERNSHIPS + ") reached.");
        }
        if (slotCount < 1 || slotCount > MAX_SLOTS) {
            throw new IllegalArgumentException("Slot count must be between 1 and " + MAX_SLOTS);
        }
        Internship internship = new Internship(title, description, companyName, this);
        internship.setLevel(level);
        internship.setPreferredMajor(preferredMajor);
        internship.setOpenDate(openDate);
        internship.setCloseDate(closeDate);
        for (int i = 1; i <= slotCount; i++) {
            internship.addSlot(new InternshipSlot(i));
        }
        manager.submitInternship(internship);
        internships.add(internship);
        return internship;
    }

    public List<Application> viewApplications(InternshipManager manager, Internship internship) {
        if (manager == null) {
            throw new IllegalArgumentException("Internship manager required.");
        }
        if (internship == null || !internships.contains(internship)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(internship.getApplications());
    }

    public void toggleVisibility(InternshipManager manager, Internship internship, boolean on) {
        ensureApproved();
        if (manager == null) {
            throw new IllegalArgumentException("Internship manager required.");
        }
        if (internship == null || !internships.contains(internship)) {
            throw new IllegalArgumentException("Internship not managed by this representative.");
        }
        internship.toggleVisibility(on);
        if (on && internship.getStatus() == InternshipStatus.PENDING) {
            manager.approveInternship(internship);
        }
        if (!on && internship.getStatus() == InternshipStatus.APPROVED) {
            internship.setStatus(InternshipStatus.PENDING);
        }
    }

    public List<Internship> getInternships(InternshipManager manager) {
        if (manager != null) {
            return manager.getInternshipsForRep(this);
        }
        return Collections.unmodifiableList(internships);
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    private void ensureApproved() {
        if (!approved) {
            throw new IllegalStateException("Company representative has not been approved yet.");
        }
    }
}
