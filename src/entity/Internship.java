import java.util.ArrayList;

public class Internship {

    private boolean approved;
    private boolean visibility;

    private String companyName;
    private String title;
    private String description;
    private String level;
    private String preferredMajor;
    private String openDate;   
    private String closeDate;  
    private String reps;

    private int numSlots;
    private ArrayList<Student> assignedStudents = new ArrayList<>();

    public Internship() {}

    public Internship(boolean approved,
                      boolean visibility,
                      String companyName,
                      String title,
                      String description,
                      String level,
                      String preferredMajor,
                      String openDate,
                      String closeDate,
                      String reps,
                      int numSlots) {

        this.approved = approved;
        this.visibility = visibility;
        this.companyName = companyName;
        this.title = title;
        this.description = description;
        this.level = level;
        this.preferredMajor = preferredMajor;
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.reps = reps;
        this.numSlots = numSlots;

    }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public boolean isVisibility() { return visibility; }
    public void setVisibility(boolean visibility) { this.visibility = visibility; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getPreferredMajor() { return preferredMajor; }
    public void setPreferredMajor(String preferredMajor) { this.preferredMajor = preferredMajor; }

    public String getOpenDate() { return openDate; }
    public void setOpenDate(String openDate) { this.openDate = openDate; }

    public String getCloseDate() { return closeDate; }
    public void setCloseDate(String closeDate) { this.closeDate = closeDate; }

    public String getReps() { return reps; }
    public void setReps(String reps) { this.reps = reps; }

    public int getNumSlots() { return numSlots; }
    public void setNumSlots(int numSlots) { this.numSlots = numSlots; }

    public ArrayList<Student> getAssignedStudents() { return assignedStudents; }
    public void setAssignedStudents(ArrayList<Student> assignedStudents) {
        this.assignedStudents = assignedStudents != null ? assignedStudents : new ArrayList<>();
    }
}