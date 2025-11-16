package modular.model;

import java.util.ArrayList;
import java.util.List;

public class ModCompany {
    private String companyName;
    private final List<ModCompanyRep> reps = new ArrayList<>();
    private final List<ModInternship> internships = new ArrayList<>();

    public ModCompany() {}

    public ModCompany(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public List<ModCompanyRep> getReps() {
        return reps;
    }

    public List<ModInternship> getInternships() {
        return internships;
    }

    public void addRep(ModCompanyRep rep) {
        if (rep == null || reps.contains(rep)) return;
        reps.add(rep);
        rep.setCompany(this);
    }

    public void addInternship(ModInternship internship) {
        if (internship == null || internships.contains(internship)) return;
        internships.add(internship);
        internship.setCompany(this);
    }
}
