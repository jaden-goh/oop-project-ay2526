package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Student extends User {
    private String year;
    private String major;

    private final List<Application> applications = new ArrayList<>();

    public Student(String userId, String name, String password, String year, String major) {
        super(userId, name, password);
        this.year = year;
        this.major = major;
    }
    


    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public List<Application> getApplications(){return applications;}

    // functions

    public boolean applyInternship(Internship internship) {
        if (internship == null) return false;
        if (!canApply()) return false;

        String id = "APP-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();

        Application app = new Application(id, this, internship, LocalDate.now());

        addApplication(app);
        return true;

    }

    public void viewApplications() {
        if (applications.isEmpty()) {
            System.out.println("You have not submitted any applications.");
        } else {
            System.out.println("Your applications:");
            for (int i = 0; i < applications.size(); i++) {
                Application application = applications.get(i);
                String title = application.getInternship() != null
                        ? application.getInternship().getTitle()
                        : "Unknown Internship";
                System.out.printf("%d. %s - Status: %s (Applied on %s)%n",
                        i + 1,
                        title,
                        application.getStatus(),
                        application.getDateApplied());
            }
        }
    }


    public boolean acceptOffer(Application application) {
        if (application == null) return false;
        if (!applications.contains(application)) return false;
        if (application.getStatus() != ApplicationStatus.SUCCESSFUL) return false;
        if (hasAcceptedPlacement()) return false;

        application.markAccepted();
        application.updateStatus(ApplicationStatus.ACCEPTED);

        for (Application other : applications) {
            if (other != application) {
                ApplicationStatus st = other.getStatus();
                if (st == ApplicationStatus.PENDING || st == ApplicationStatus.ACCEPTED) {
                    other.updateStatus(ApplicationStatus.WITHDRAWN);
                }
            }
        }

        return true;
    }
    public boolean reqWithdrawal() {
        if (applications.isEmpty()) {
            System.out.println("You have no applications to withdraw.");
            return false;
        }

        System.out.println("Select an application to request withdrawal:");
        for (int i = 0; i < applications.size(); i++) {
            Application application = applications.get(i);
            System.out.printf("%d. %s (%s)%n",
                    i + 1,
                    application.getInternship().getTitle(),
                    application.getStatus());
        }
        System.out.println("Enter choice: ");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();
        int selection;
        try {
            selection = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return false;
        }

        if (selection < 1 || selection > applications.size()) {
            System.out.println("Invalid selection.");
            return false;
        }

        Application selected = applications.get(selection - 1);
        ApplicationStatus status = selected.getStatus();
        if (status == ApplicationStatus.WITHDRAWN) {
            System.out.println("This application cannot be withdrawn.");
            return false;
        }

        selected.setWithdrawn(true);
        selected.updateStatus(ApplicationStatus.PENDING_WITHDRAWAL);
        System.out.println("Withdrawal requested for " + selected.getInternship().getTitle() + ".");
        return true;
    }
    public boolean canApply() {
        long activeCount = applications.stream()
                .map(Application::getStatus)
                .filter(st -> st == ApplicationStatus.PENDING || st == ApplicationStatus.SUCCESSFUL)
                .count();
        int maxApplications = 3;
        return activeCount < maxApplications && !hasAcceptedPlacement();
    }

    public boolean hasAcceptedPlacement() {
        return applications.stream().anyMatch(Application::isAccepted);
    }

    public void addApplication(Application application) {
        if (application != null) applications.add(application);
    }

    public void displayDetails() {
        System.out.println("Student ID: " + getId());
        System.out.println("Name: " + getName());
        System.out.println("Major: " + getMajor());
        System.out.println("Year: " + getYear());
        System.out.println("Applications submitted: " + applications.size());
        System.out.println("Password: " + getPassword());
    }

}
