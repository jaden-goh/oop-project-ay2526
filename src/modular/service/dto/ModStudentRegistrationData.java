package modular.service.dto;

public class ModStudentRegistrationData {
    private final String studentId;
    private final String name;
    private final String major;
    private final String year;
    private final String email;

    public ModStudentRegistrationData(String studentId, String name, String major, String year, String email) {
        this.studentId = studentId;
        this.name = name;
        this.major = major;
        this.year = year;
        this.email = email;
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getMajor() { return major; }
    public String getYear() { return year; }
    public String getEmail() { return email; }
}
