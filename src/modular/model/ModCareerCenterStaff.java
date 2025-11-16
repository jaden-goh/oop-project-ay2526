package modular.model;

public class ModCareerCenterStaff extends ModUser {
    private String role;
    private String department;

    public ModCareerCenterStaff(String id, String name, String password, String role, String department) {
        super(id, name, password);
        this.role = role;
        this.department = department;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
