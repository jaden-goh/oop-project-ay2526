public class Student extends User {
    private int year;
    private String major;

    public Student(String id, String name, String password, int year, String major) {
        super(id, name, password);
        this.year = year;
        this.major = major;
    }
    public boolean viewOpportunities() { return true; }
    public void applyIntern() { }
    public void viewInternship() { }
    public void requestWithdrawal() { }
}

