// documented

package boundary;

import control.InternshipManager;
import entity.FilterCriteria;
import entity.Internship;
import entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


/**
 * Boundary class that handles user interaction for browsing and filtering
 * internship opportunities.
 *
 * <p>Supports:</p>
 * <ul>
 *     <li>Prompting the user for filter criteria</li>
 *     <li>Applying saved filter preferences</li>
 *     <li>Applying dynamic filters (status, major, level, closing date)</li>
 *     <li>Applying additional functional filters (Predicates)</li>
 * </ul>
 *
 * <p>This class does not handle filtering logic itself—filtering is delegated to
 * {@link InternshipManager} via {@link InternshipManager#filter(FilterCriteria)}.</p>
 */

public class InternshipBrowser {

    /** Controls internship retrieval and filtering logic. */
    private final InternshipManager internshipManager;

    /** Helper utility for console-based user interaction. */
    private final ConsoleHelper console;

    /**
     * Creates a browser instance for listing internships.
     *
     * @param internshipManager manager used to retrieve internships
     * @param console           console helper for user input
     */
    public InternshipBrowser(InternshipManager internshipManager, ConsoleHelper console) {
        this.internshipManager = internshipManager;
        this.console = console;
    }

    /**
     * Retrieves internships filtered by the user's chosen filter criteria, and
     * optionally an additional functional filter.
     *
     * @param user              the user performing the search
     * @param additionalFilter  extra predicate-based filter (may be null)
     * @return filtered list of internships
     */
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

    /**
     * Interactively prompts the user to set or load filtering options.
     *
     * <p>Workflow:</p>
     * <ol>
     *     <li>If user has saved filters, optionally reuse them.</li>
     *     <li>Otherwise, prompt for new filter options (status, major, level, closing date).</li>
     *     <li>Optionally save the new filters.</li>
     * </ol>
     *
     * @param user filter owner (may store preferences)
     * @return a {@link FilterCriteria} instance, or null if no filters selected
     */
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

    /**
     * Checks whether at least one of the filter fields is populated.
     *
     * @param criteria the filter criteria to validate
     * @return true if at least one filter field has a value
     */
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

    /**
     * Returns a readable string describing the filter criteria.
     *
     * @param criteria the criteria to describe
     * @return user-friendly filter summary
     */
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
