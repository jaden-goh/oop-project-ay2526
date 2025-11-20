// documented

package entity;

import java.time.LocalDate;

/**
 * Represents a set of optional filtering criteria used to determine whether
 * an {@link Internship} matches user-selected filters. Criteria include:
 * <ul>
 *     <li>Internship status</li>
 *     <li>Preferred major</li>
 *     <li>Internship level</li>
 *     <li>Closing date</li>
 * </ul>
 *
 * <p>All fields are optional. If a field is {@code null}, that criterion is ignored
 * during filtering.</p>
 */

public class FilterCriteria {

    /** Internship status. */
    private InternshipStatus status;

    /** Preferred major filter. */
    private String preferredMajor;

    /** Internship level filter. */
    private InternshipLevel level;

    /** Latest acceptable closing date. */
    private LocalDate closingDate;

    /**
     * Creates an empty filter criteria instance with no filters applied.
     */
    public FilterCriteria() {}

    /**
     * Creates a filter criteria instance with the specified values.
     * Any parameter may be {@code null} to indicate no filtering on that field.
     *
     * @param status         desired internship status
     * @param preferredMajor preferred student major
     * @param level          required internship level
     * @param closingDate    latest allowed closing date
     */
    public FilterCriteria(InternshipStatus status, String preferredMajor,
                          InternshipLevel level, LocalDate closingDate) {
        this.status = status;
        this.preferredMajor = preferredMajor;
        this.level = level;
        this.closingDate = closingDate;
    }

    /**
     * Returns the selected internship status criterion.
     *
     * @return the status filter, or {@code null} if not applied
     */
    public InternshipStatus getStatus() {
        return status;
    }

    /**
     * Sets the internship status filter.
     *
     * @param status the status to filter by, or {@code null} to disable filtering
     */
    public void setStatus(InternshipStatus status) {
        this.status = status;
    }

    /**
     * Returns the preferred major filter.
     *
     * @return the preferred major, or {@code null} if not applied
     */
    public String getPreferredMajor() {
        return preferredMajor;
    }

    /**
     * Sets the preferred major criterion.
     *
     * @param preferredMajor major to filter by, or {@code null} to disable filtering
     */
    public void setPreferredMajor(String preferredMajor) {
        this.preferredMajor = preferredMajor;
    }

    /**
     * Returns the internship level criterion.
     *
     * @return the internship level filter, or {@code null} if not applied
     */
    public InternshipLevel getLevel() {
        return level;
    }

    /**
     * Sets the internship level filter.
     *
     * @param level the required level, or {@code null} to disable filtering
     */
    public void setLevel(InternshipLevel level) {
        this.level = level;
    }


    /**
     * Returns the closing date filter.
     *
     * @return the latest allowed closing date, or {@code null} if not applied
     */
    public LocalDate getClosingDate() {
        return closingDate;
    }

    /**
     * Sets the closing date filter.
     *
     * @param closingDate the upper limit for internship closing dates
     */
    public void setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
    }

    /**
     * Determines whether the given internship matches all non-null filter criteria.
     *
     * @param internship the internship to test
     * @return {@code true} if it matches the criteria, otherwise {@code false}
     */
    public boolean matches(Internship internship) {
        if (internship == null) {
            return false;
        }
        if (status != null && internship.getStatus() != status) {
            return false;
        }
        if (preferredMajor != null && !preferredMajor.isBlank()
                && !internship.acceptsMajor(preferredMajor)) {
            return false;
        }
        if (level != null && internship.getLevel() != level) {
            return false;
        }
        if (closingDate != null && internship.getCloseDate() != null
                && internship.getCloseDate().isAfter(closingDate)) {
            return false;
        }
        return true;
    }
}
