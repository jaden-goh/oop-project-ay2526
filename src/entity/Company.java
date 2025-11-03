public class Company {
    private String companyName;
    private int numInternships;

    public Company() {}

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public int getNumInternships() { return numInternships; }
    public void setNumInternships(int numInternships) { this.numInternships = numInternships; }

    // functions
    public void addRep(CompanyRep rep) { }
    public void addInternship(Internship internship) { }
}

