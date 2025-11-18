import java.util.ArrayList;
import java.util.List;

public class ReportGenerator {
    public void generateByStatus(List<Internship> internships, InternshipStatus status) {
        List<Internship> matches = new ArrayList<>();
        for (Internship internship : internships) {
            if (internship.getStatus() == status) {
                matches.add(internship);
            }
        }
        printReport("Internships with status " + status, matches);
    }

    public void generateByMajor(List<Internship> internships, String major) {
        List<Internship> matches = new ArrayList<>();
        for (Internship internship : internships) {
            if (internship.getPreferredMajor() != null
                    && internship.getPreferredMajor().equalsIgnoreCase(major)) {
                matches.add(internship);
            }
        }
        printReport("Internships filtered by major: " + major, matches);
    }

    public void generateByLevel(List<Internship> internships, InternshipLevel level) {
        List<Internship> matches = new ArrayList<>();
        for (Internship internship : internships) {
            if (internship.getLevel() == level) {
                matches.add(internship);
            }
        }
        printReport("Internships for level " + level, matches);
    }

    public void generateCompanySummary(List<Internship> internships, String company) {
        List<Internship> matches = new ArrayList<>();
        for (Internship internship : internships) {
            if (internship.getCompanyName() != null
                    && internship.getCompanyName().equalsIgnoreCase(company)) {
                matches.add(internship);
            }
        }
        printReport("Internships offered by " + company, matches);
    }

    private void printReport(String title, List<Internship> internships) {
        System.out.println("\n=== " + title + " ===");
        if (internships.isEmpty()) {
            System.out.println("No internships found for this report.");
            return;
        }
        int index = 1;
        for (Internship internship : internships) {
            int totalSlots = internship.getSlots().size();
            long filledSlots = internship.getSlots().stream()
                    .filter(slot -> slot.getAssignedStudent() != null)
                    .count();
            System.out.println(index++ + ". " + internship.getTitle()
                    + " (" + internship.getCompanyName() + ")"
                    + " | Status: " + internship.getStatus()
                    + " | Level: " + internship.getLevel()
                    + " | Major: " + (internship.getPreferredMajor() == null ? "Any" : internship.getPreferredMajor())
                    + " | Visible: " + (internship.isVisible() ? "Yes" : "No")
                    + " | Slots: " + filledSlots + "/" + totalSlots);
        }
        long visibleCount = internships.stream().filter(Internship::isVisible).count();
        System.out.println("Total internships: " + internships.size()
                + " (visible: " + visibleCount + ")");
    }
}
