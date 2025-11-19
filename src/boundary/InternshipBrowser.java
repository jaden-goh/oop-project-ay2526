package boundary;

import control.InternshipManager;
import entity.FilterCriteria;
import entity.Internship;
import entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class InternshipBrowser {
    private final InternshipManager internshipManager;
    private final ConsoleHelper console;

    public InternshipBrowser(InternshipManager internshipManager, ConsoleHelper console) {
        this.internshipManager = internshipManager;
        this.console = console;
    }

    public List<Internship> fetchFilteredInternships(User user, Predicate<Internship> additionalFilter) {
        FilterCriteria criteria = promptFilterCriteria(user);
        List<Internship> internships = internshipManager.filter(criteria);
        if (additionalFilter == null) {
            return internships;
        }
        List<Internship> filtered = new ArrayList<>();
        for (Internship internship : internships) {
            if (additionalFilter.test(internship)) {
                filtered.add(internship);
            }
        }
        return filtered;
    }

    private FilterCriteria promptFilterCriteria(User user) {
        if (user == null) {
            return null;
        }
        FilterCriteria saved = user.getFilterPreferences();
        if (saved != null && hasFilterValues(saved)) {
            System.out.println("Saved filters: " + describeFilterCriteria(saved));
            if (console.promptYesNo("Use saved filters? (y/n): ", true)) {
                return saved;
            }
        }
        if (!console.promptYesNo("Apply filters to this list? (y/n): ", false)) {
            return null;
        }
        FilterCriteria criteria = new FilterCriteria();
        if (console.promptYesNo("Filter by status? (y/n): ", false)) {
            criteria.setStatus(console.promptStatusSelection());
        }
        String major = console.readLine("Filter by preferred major (leave blank for any): ");
        if (!major.isEmpty()) {
            criteria.setPreferredMajor(major);
        }
        if (console.promptYesNo("Filter by internship level? (y/n): ", false)) {
            criteria.setLevel(console.promptInternshipLevel());
        }
        if (console.promptYesNo("Filter by closing date? (y/n): ", false)) {
            LocalDate closingDate = console.readOptionalDate("Latest closing date (yyyy-MM-dd, blank to cancel): ");
            if (closingDate != null) {
                criteria.setClosingDate(closingDate);
            }
        }
        if (!hasFilterValues(criteria)) {
            System.out.println("No filters applied. Showing all internships.");
            return null;
        }
        if (console.promptYesNo("Save these filters for future sessions? (y/n): ", true)) {
            user.setFilterPreferences(criteria);
        }
        return criteria;
    }

    private boolean hasFilterValues(FilterCriteria criteria) {
        if (criteria == null) {
            return false;
        }
        if (criteria.getStatus() != null || criteria.getLevel() != null || criteria.getClosingDate() != null) {
            return true;
        }
        String preferredMajor = criteria.getPreferredMajor();
        return preferredMajor != null && !preferredMajor.isBlank();
    }

    private String describeFilterCriteria(FilterCriteria criteria) {
        if (criteria == null) {
            return "(none)";
        }
        List<String> parts = new ArrayList<>();
        if (criteria.getStatus() != null) {
            parts.add("Status=" + criteria.getStatus());
        }
        if (criteria.getPreferredMajor() != null && !criteria.getPreferredMajor().isBlank()) {
            parts.add("Major=" + criteria.getPreferredMajor());
        }
        if (criteria.getLevel() != null) {
            parts.add("Level=" + criteria.getLevel());
        }
        if (criteria.getClosingDate() != null) {
            parts.add("Closing on/before " + criteria.getClosingDate());
        }
        return parts.isEmpty() ? "(none)" : String.join(", ", parts);
    }
}
