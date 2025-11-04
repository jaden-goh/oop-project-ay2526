package entity;

public class Student extends User {
    private int year;
    private String major;

    public Student(String id, String name, String password, int year, String major) {
        super(id, name, password);
        this.year = year;
        this.major = major;
    }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    // functions
    public boolean viewOpportunities() { return false; }
    public boolean applyInternship(Internship internship) { return false; }
    public boolean acceptOffer(Application application) { return false; }
    public boolean reqWithdrawal(Application application) { return false; }
    public boolean canApply() { return false; }
}

