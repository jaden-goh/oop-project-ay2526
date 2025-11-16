package modular.service.dto;

public class ModStaffRegistrationData {
    private final String staffId;
    private final String name;
    private final String role;
    private final String department;
    private final String email;

    public ModStaffRegistrationData(String staffId, String name, String role, String department, String email) {
        this.staffId = staffId;
        this.name = name;
        this.role = role;
        this.department = department;
        this.email = email;
    }

    public String getStaffId() { return staffId; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
    public String getEmail() { return email; }
}
