package modular.model;

import java.util.ArrayList;
import java.util.List;

public class ModCompanyRep extends ModUser {
    private ModCompany company;
    private String department;
    private String position;
    private boolean authorised;
    private final List<ModInternship> internships = new ArrayList<>();

    public ModCompanyRep(String id, String name, String password) {
        super(id, name, password);
    }

    public ModCompany getCompany() { return company; }
    public void setCompany(ModCompany company) { this.company = company; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public boolean isAuthorised() { return authorised; }
    public void setAuthorised(boolean authorised) { this.authorised = authorised; }

    public List<ModInternship> getInternships() { return internships; }
}
