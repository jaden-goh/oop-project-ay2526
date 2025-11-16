package modular.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ModInternship {
    private String title;
    private String description;
    private String level;
    private String preferredMajor;
    private ModInternshipStatus status = ModInternshipStatus.PENDING;
    private Date openDate;
    private Date closeDate;
    private boolean visible;
    private boolean approved;
    private ModCompany company;
    private ModCompanyRep rep;
    private final List<ModInternshipSlot> slots = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getPreferredMajor() { return preferredMajor; }
    public void setPreferredMajor(String preferredMajor) { this.preferredMajor = preferredMajor; }

    public ModInternshipStatus getStatus() { return status; }
    public void setStatus(ModInternshipStatus status) { this.status = status; }

    public Date getOpenDate() { return openDate; }
    public void setOpenDate(Date openDate) { this.openDate = openDate; }

    public Date getCloseDate() { return closeDate; }
    public void setCloseDate(Date closeDate) { this.closeDate = closeDate; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public ModCompany getCompany() { return company; }
    public void setCompany(ModCompany company) { this.company = company; }

    public ModCompanyRep getRep() { return rep; }
    public void setRep(ModCompanyRep rep) { this.rep = rep; }

    public List<ModInternshipSlot> getSlots() { return slots; }

    public void addSlot(ModInternshipSlot slot) {
        if (slot != null) {
            slots.add(slot);
        }
    }

    public boolean isFull() {
        return slots.stream().allMatch(ModInternshipSlot::isFilled);
    }

    public boolean isOpen(Date now) {
        return visible && approved &&
                status == ModInternshipStatus.APPROVED &&
                now.compareTo(openDate) >= 0 &&
                now.compareTo(closeDate) <= 0;
    }
}
