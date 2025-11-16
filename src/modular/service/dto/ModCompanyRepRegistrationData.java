package modular.service.dto;

public class ModCompanyRepRegistrationData {
    private final String repId;
    private final String name;
    private final String companyName;
    private final String department;
    private final String position;
    private final String email;

    public ModCompanyRepRegistrationData(String repId,
                                         String name,
                                         String companyName,
                                         String department,
                                         String position,
                                         String email) {
        this.repId = repId;
        this.name = name;
        this.companyName = companyName;
        this.department = department;
        this.position = position;
        this.email = email;
    }

    public String getRepId() { return repId; }
    public String getName() { return name; }
    public String getCompanyName() { return companyName; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }
    public String getEmail() { return email; }
}
