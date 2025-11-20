// documented

package control;

import entity.Internship;
import entity.InternshipLevel;
import entity.InternshipStatus;
import entity.InternshipSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides reporting utilities for internship data.
 *
 * <p>This class generates simple console-based reports filtered by:</p>
 * <ul>
 *     <li>Internship status</li>
 *     <li>Preferred major</li>
 *     <li>Internship level (Basic / Intermediate / Advanced)</li>
 *     <li>Company name</li>
 * </ul>
 *
 * <p>The reports summarize visibility, level, status, preferred major,
 * and slot occupancy for each internship.</p>
 */

public class ReportGenerator {

    /**
     * Generates and prints a report of internships filtered by status.
     *
     * @param internships the list of internships to evaluate
     * @param status      the status to filter by
     */
    public void generateByStatus(List<Internship> internships, InternshipStatus status) {
        List<Internship> matches = new ArrayList<>();
        for (Internship internship : internships) {
            if (internship.getStatus() == status) {
                matches.add(internship);
            }
        }
        printReport("Internships with status " + status, matches);
    }

    /**
     * Generates a report of internships filtered by preferred major.
     *
     * <p>Note: {@link Internship#acceptsMajor(String)} returns true when:</p>
     * <ul>
     *     <li>The internship has no preferred major (i.e., open to all), or</li>
     *     <li>The provided major matches the internship's preferred major.</li>
     * </ul>
     *
     * @param internships the list of internships to evaluate
     * @param major       the major to filter by
     */
    public void generateByMajor(List<Internship> internships, String major) {
        List<Internship> matches = new ArrayList<>();
        for (Internship internship : internships) {
            if (internship.acceptsMajor(major)) {
                matches.add(internship);
            }
        }
        printReport("Internships filtered by major: " + major, matches);
    }

    /**
     * Generates a report of internships filtered by level.
     *
     * @param internships the list of internships to evaluate
     * @param level       the internship level to filter by
     */
    public void generateByLevel(List<Internship> internships, InternshipLevel level) {
        List<Internship> matches = new ArrayList<>();
        for (Internship internship : internships) {
            if (internship.getLevel() == level) {
                matches.add(internship);
            }
        }
        printReport("Internships for level " + level, matches);
    }

    /**
     * Generates a report summarizing internships offered by a specific company.
     *
     * @param internships the list of internships to evaluate
     * @param company      company name to filter by (case-insensitive)
     */
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

    /**
     * Prints a summary report for the provided list of internships.
     *
     * <p>Each internship entry includes:</p>
     * <ul>
     *     <li>Title and company name</li>
     *     <li>Status and level</li>
     *     <li>Preferred major</li>
     *     <li>Visibility (Yes / No)</li>
     *     <li>Slot occupancy (filled / total)</li>
     * </ul>
     *
     * @param title       label/title for the report
     * @param internships list of internships to include
     */
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
                    + " | Major: " + formatPreferredMajor(internship)
                    + " | Visible: " + (internship.isVisible() ? "Yes" : "No")
                    + " | Slots: " + filledSlots + "/" + totalSlots);
        }
        long visibleCount = internships.stream().filter(Internship::isVisible).count();
        System.out.println("Total internships: " + internships.size()
                + " (visible: " + visibleCount + ")");
    }

    /**
     * Helper method to provide a readable preferred-major summary.
     *
     * @param internship the internship to extract the major from
     * @return "Any" if no preferred major is set, else the preferred major
     */
    private String formatPreferredMajor(Internship internship) {
        if (internship == null) {
            return "Any";
        }
        String major = internship.getPreferredMajor();
        return major == null || major.isBlank() ? "Any" : major;
    }
}
