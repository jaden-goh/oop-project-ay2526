import java.util.ArrayList;

public class CompanyRep extends User {
    private String companyName;
    private boolean Authorised;
    private ArrayList<Internship> Internships;


    public CompanyRep(String id, String name, String password, String companyName, boolean Authorised, ArrayList<Internship> Internships) {
        super(id, name, password);
        this.companyName = companyName;
        this.Authorised = Authorised;
        this.Internships = Internships;
    }
    
    public boolean register() {return true;}
    public void createInternship() { }
    public void toggleInternship(Internship Internship) { }
}


