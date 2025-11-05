package entity;
import java.util.ArrayList;
import java.util.Date;

public class Internship {

    private String title;
    private String description;
    private String level;
    private String preferredMajor;
    private InternshipStatus status;
    private Date openDate;
    private Date closeDate;
    private boolean visibility;
    private boolean approved;

    private Company company;
    private CompanyRep rep;

    private int numSlots;
    private ArrayList<InternshipSlot> slots = new ArrayList<>(numSlots);

    public Internship() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getPreferredMajor() { return preferredMajor; }
    public void setPreferredMajor(String preferredMajor) { this.preferredMajor = preferredMajor; }

    public InternshipStatus getStatus() { return this.status; }
    public void setStatus(String statusStr) {
    try {
        this.status = InternshipStatus.valueOf(statusStr.toUpperCase());
    } catch (IllegalArgumentException e) {
        System.err.println("Invalid status: " + statusStr);
    }
}

    public Date getOpenDate() { return openDate; }
    public void setOpenDate(Date openDate) { this.openDate = openDate; }

    public Date getCloseDate() { return closeDate; }
    public void setCloseDate(Date closeDate) { this.closeDate = closeDate; }

    public boolean isVisibility() { return visibility; }
    public void setVisibility(boolean visibility) { this.visibility = visibility; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public CompanyRep getRep() { return rep; }
    public void setRep(CompanyRep rep) { this.rep = rep; }

    public int getNumSlots() { return numSlots; }
    public void setNumSlots(int numSlots) { this.numSlots = numSlots; }

    public ArrayList<InternshipSlot> getSlots() { return slots; }
    public void setSlots(ArrayList<InternshipSlot> slots) { this.slots = slots != null ? slots : new ArrayList<>(); }

    // functions

    public void addSlot(InternshipSlot internshipSlot) {
        if (!isFull()) {
            this.slots.add(internshipSlot);
        }
    }

    public boolean isOpen() {
        Date now = new Date();
        return visibility &&
            approved &&
            status == InternshipStatus.APPROVED &&
            now.compareTo(openDate) >= 0 &&
            now.compareTo(closeDate) <= 0;
    }

    public boolean assignStudent(Application application) {
        for (InternshipSlot slot : slots) {
            if (!slot.isFilled()) {
                slot.assignStudent(application);
                return true;
            }
        }
        return false; // No empty slot found
    }

    public boolean isFull() {
        for (InternshipSlot slot : slots) {
            if (!slot.isFilled()) {
                return false;
            }
        }
        return true;
    }

    public void updateStatus(InternshipStatus newStatus) {
        this.status = newStatus;
}

}   
