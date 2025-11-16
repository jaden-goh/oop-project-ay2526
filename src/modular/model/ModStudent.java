package modular.model;

import java.util.ArrayList;
import java.util.List;

public class ModStudent extends ModUser {
    private String year;
    private String major;
    private final List<ModApplication> applications = new ArrayList<>();

    public ModStudent(String id, String name, String password, String year, String major) {
        super(id, name, password);
        this.year = year;
        this.major = major;
    }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public List<ModApplication> getApplications() { return applications; }

    public void addApplication(ModApplication application) {
        if (application != null) {
            applications.add(application);
        }
    }
}
