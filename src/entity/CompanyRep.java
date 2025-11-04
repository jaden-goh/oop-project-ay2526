package entity;
import java.util.ArrayList;

public class CompanyRep extends User {
    private Company company;
    private boolean authorised;
    private ArrayList<Internship> internships;

    public CompanyRep(String id, String name, String password) {
        super(id, name, password);
        this.authorised = false;
        this.internships = new ArrayList<>();
    }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public boolean isAuthorised() { return authorised; }
    public void setAuthorised(boolean authorised) { this.authorised = authorised; }

    public ArrayList<Internship> getInternships() { return internships; }
    public void setInternships(ArrayList<Internship> internships) { this.internships = internships; }

    // functions
    public boolean register() { return false; }
    public Internship createInternship(String title, String description, String level, String preferredMajor,
                                       java.util.Date openDate, java.util.Date closeDate, int numSlots) { return null; }
    public void toggleVisibility(Internship internship) { }
    public boolean approveApplication(Application application) { return false; }
    public boolean rejectApplication(Application application) { return false; }
    public java.util.List<Application> viewApplications(Internship internship) { return null; }
}


