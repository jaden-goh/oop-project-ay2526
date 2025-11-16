package entity;

import java.util.ArrayList;
import java.util.List;

public class Company {
    private String companyName;
    private final List<CompanyRep> reps = new ArrayList<>();
    private final List<Internship> internships = new ArrayList<>();

    public Company() {}

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    // (i changed this a little thought it would be better to derive from list so the number is never outdated) 
    public int getNumInternships() { return internships.size(); }

    public List<CompanyRep> getReps() { return reps; }
    public List<Internship> getInternships() { return internships; }

    // functions

    // adds a rep and links the rep back to this company
    public void addRep(CompanyRep rep) {
        
        // ignore if null 
        if (rep == null) 
            { return; }
        
        // prevent duplicates i.e. same rep from behind added twice 
        if (!reps.contains(rep)) {
            reps.add(rep);

            // link back: the rep's company should be THIS company
            rep.setCompany(this); 
        }
    }

    // adds an internship and links it back to this company

    public void addInternship(Internship internship) {
        
        // ignore if null
        if (internship == null) 
            { return; }

        // prevent duplicates i.e. same rep from behind added twice 
        if (!internships.contains(internship)) {
            internships.add(internship);

            // link back: the rep's company should be THIS company
            internship.setCompany(this); 
        }
    }

    public double getApprovedApplicationRate() {
        int total = 0;
        int approved = 0;
        for (Internship internship : internships) {
            for (InternshipSlot slot : internship.getSlots()) {
                Application app = slot.getApplication();
                if (app != null) {
                    total++;
                    if (app.getStatus() == ApplicationStatus.SUCCESSFUL) {
                        approved++;
                    }
                }
            }
        }
        if (total == 0) return 0;
        return approved * 1.0 / total;
    }
}

