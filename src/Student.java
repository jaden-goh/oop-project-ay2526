import java.util.ArrayList;
import java.util.List;

public class Student extends User {
    private final int yearOfStudy;
    private final String major;
    private final List<Application> applications = new ArrayList<>();
    private Application acceptedPlacement;

    public Student(String userID, String name, String password, int yearOfStudy, String major) {
        super(userID, name, password);
        this.yearOfStudy = yearOfStudy;
        this.major = major;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public String getMajor() {
        return major;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public boolean apply(Internship internship, ApplicationManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("Application manager required.");
        }
        try {
            return manager.submitApplication(this, internship) != null;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public WithdrawalRequest withdraw(Application application, WithdrawalManager manager, String reason) {
        if (manager == null) {
            throw new IllegalArgumentException("Withdrawal manager required.");
        }
        if (application == null || !applications.contains(application)) {
            throw new IllegalArgumentException("Application does not belong to student.");
        }
        return manager.submitRequest(application, reason);
    }

    public void acceptPlacement(Application application, ApplicationManager manager) {
        if (application == null || !applications.contains(application)) {
            throw new IllegalArgumentException("Application does not belong to student.");
        }
        if (manager == null) {
            throw new IllegalArgumentException("Application manager required.");
        }
        acceptedPlacement = application;
        for (Application other : applications) {
            if (other != application && (other.getStatus() == ApplicationStatus.PENDING || other.getStatus() == ApplicationStatus.SUCCESSFUL)) {
                    manager.updateStatus(other, ApplicationStatus.UNSUCCESSFUL);
                }
        }
        manager.updateStatus(application, ApplicationStatus.SUCCESSFUL, true);
    }

    public boolean hasAcceptedPlacement() {
        return acceptedPlacement != null;
    }

    public Application getAcceptedPlacement() {
        return acceptedPlacement;
    }

}
