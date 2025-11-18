import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationManager {
    private static final int MAX_ACTIVE_APPLICATIONS = 3;
    private final List<String> submissionNotifications = new ArrayList<>();
    private String lastFailureReason = "";

    public Application submitApplication(Student student, Internship internship) {
        if (student == null || internship == null) {
            throw new IllegalArgumentException("Student and internship are required.");
        }
        if (!enforceRules(student, internship)) {
            throw new IllegalStateException(lastFailureReason);
        }
        Application application = new Application(student, internship);
        internship.addApplication(application);
        student.getApplications().add(application);
        submissionNotifications.add(LocalDateTime.now() + " :: "
                + student.getName() + " applied for " + internship.getTitle());
        lastFailureReason = "";
        return application;
    }

    public void updateStatus(Application application, ApplicationStatus status) {
        updateStatus(application, status, false);
    }

    public void updateStatus(Application application, ApplicationStatus status, boolean confirmOffer) {
        if (application == null || status == null) {
            return;
        }
        application.setStatus(status);
        if (status == ApplicationStatus.SUCCESSFUL) {
            markOtherApplications(application);
            if (confirmOffer) {
                assignSlot(application);
            }
        } else if (status == ApplicationStatus.UNSUCCESSFUL) {
            releaseSlot(application);
        }
    }

    public boolean enforceRules(Student student, Internship internship) {
        if (student == null || internship == null) {
            lastFailureReason = "Student and internship are required.";
            return false;
        }
        if (internship.getStatus() != InternshipStatus.APPROVED) {
            lastFailureReason = "Internship has not been approved yet.";
            return false;
        }
        if (!internship.isVisible()) {
            lastFailureReason = "Internship is currently hidden.";
            return false;
        }
        String preferredMajor = internship.getPreferredMajor();
        String studentMajor = student.getMajor();
        if (preferredMajor != null && !preferredMajor.isBlank()
                && studentMajor != null && !preferredMajor.equalsIgnoreCase(studentMajor)) {
            lastFailureReason = "Major does not match the preferred major for this internship.";
            return false;
        }
        InternshipLevel level = internship.getLevel();
        if (student.getYearOfStudy() <= 2 && level != null && level != InternshipLevel.BASIC) {
            lastFailureReason = "Lower-year students may only apply for BASIC level internships.";
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate openDate = internship.getOpenDate();
        if (openDate != null && today.isBefore(openDate)) {
            lastFailureReason = "Internship is not open for applications yet.";
            return false;
        }
        LocalDate closeDate = internship.getCloseDate();
        if (closeDate != null && today.isAfter(closeDate)) {
            lastFailureReason = "Internship is already closed.";
            return false;
        }
        long activeCount = student.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING
                        || app.getStatus() == ApplicationStatus.SUCCESSFUL)
                .count();
        if (activeCount >= MAX_ACTIVE_APPLICATIONS) {
            lastFailureReason = "Maximum of " + MAX_ACTIVE_APPLICATIONS + " active applications reached.";
            return false;
        }
        for (Application application : student.getApplications()) {
            if (application.getInternship() == internship
                    && application.getStatus() != ApplicationStatus.UNSUCCESSFUL) {
                lastFailureReason = "You have already applied for this internship.";
                return false;
            }
        }
        if (student.hasAcceptedPlacement()) {
            lastFailureReason = "You have already accepted a placement.";
            return false;
        }
        if (internship.isFull()) {
            lastFailureReason = "Internship slots have been filled.";
            return false;
        }
        lastFailureReason = "";
        return true;
    }

    public String getLastFailureReason() {
        return lastFailureReason;
    }

    private void assignSlot(Application application) {
        Internship internship = application.getInternship();
        for (InternshipSlot slot : internship.getSlots()) {
            if (slot.getAssignedStudent() == null) {
                slot.assignStudent(application.getStudent());
                break;
            }
        }
        if (internship.isFull()) {
            internship.setStatus(InternshipStatus.FILLED);
        }
    }

    private void releaseSlot(Application application) {
        Internship internship = application.getInternship();
        for (InternshipSlot slot : internship.getSlots()) {
            if (slot.getAssignedStudent() == application.getStudent()) {
                slot.release();
                internship.setStatus(InternshipStatus.APPROVED);
                break;
            }
        }
    }

    private void markOtherApplications(Application acceptedApplication) {
        Student student = acceptedApplication.getStudent();
        for (Application application : student.getApplications()) {
            if (application != acceptedApplication && application.getStatus() == ApplicationStatus.PENDING) {
                application.setStatus(ApplicationStatus.UNSUCCESSFUL);
            }
        }
    }

}
