// documented

package control;

import entity.CompanyRep;
import entity.FilterCriteria;
import entity.Internship;
import entity.InternshipStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the lifecycle of internship postings within the system.
 *
 * <p>This includes:</p>
 * <ul>
 *     <li>Submitting internship opportunities</li>
 *     <li>Approving or rejecting internships</li>
 *     <li>Auto-updating their status based on dates or slot availability</li>
 *     <li>Filtering internships by criteria</li>
 *     <li>Managing internships belonging to a specific company representative</li>
 * </ul>
 */

public class InternshipManager {

    /** Maximum internships a company representative may have pending/approved at one time. */
    private static final int MAX_INTERNSHIPS_PER_REP = 5;

    /** Internal list storing all internship opportunities in the system. */
    private final List<Internship> internships = new ArrayList<>();

    /**
     * Submits a new internship for approval.
     *
     * <p>Only approved company representatives may submit internships. Pending and approved
     * internships count toward the representative's internship limit.</p>
     *
     * <p>Submitted internships start with:</p>
     * <ul>
     *     <li>Status = {@link InternshipStatus#PENDING}</li>
     *     <li>Visibility = false</li>
     * </ul>
     *
     * @param internship the internship to submit
     * @return the submitted internship
     *
     * @throws IllegalArgumentException if internship is null
     * @throws IllegalStateException    if the representative is not approved or has reached the maximum quota
     */
    public void submitInternship(Internship internship) {
        if (internship == null) {
            throw new IllegalArgumentException("Internship required.");
        }
        CompanyRep rep = internship.getRepInCharge();
        if (rep == null || !rep.isApproved()) {
            throw new IllegalStateException("Only approved company representatives may submit internships.");
        }
        long activeInternships = internships.stream()
                .filter(existing -> existing.getRepInCharge() == rep
                        && existing.getStatus() != InternshipStatus.REJECTED)
                .count();
        if (activeInternships >= MAX_INTERNSHIPS_PER_REP) {
            throw new IllegalStateException("Maximum of " + MAX_INTERNSHIPS_PER_REP + " internships reached.");
        }
        internship.setStatus(InternshipStatus.PENDING);
        internship.setVisibility(false);
        internships.add(internship);
    }

    /**
     * Approves an internship, making it visible and open for student applications.
     *
     * @param internship internship to approve
     */
    public void approveInternship(Internship internship) {
        if (internship == null) {
            return;
        }
        internship.setStatus(InternshipStatus.APPROVED);
        internship.setVisibility(true);
    }

    /**
     * Rejects an internship, blocking visibility to students.
     *
     * @param internship internship to reject
     */
    public void rejectInternship(Internship internship) {
        if (internship == null) {
            return;
        }
        internship.setStatus(InternshipStatus.REJECTED);
        internship.setVisibility(false);
    }

    /**
     * Refreshes the statuses of internships:
     * <ul>
     *     <li>If the closing date has passed, an approved internship becomes FILLED</li>
     *     <li>If all slots are taken, the internship becomes FILLED</li>
     *     <li>Filled internships are hidden from students</li>
     * </ul>
     */
    public void refreshStatuses() {
        LocalDate today = LocalDate.now();
        for (Internship internship : internships) {
            LocalDate closeDate = internship.getCloseDate();
            if (closeDate != null && today.isAfter(closeDate) && internship.getStatus() == InternshipStatus.APPROVED) {
                internship.setStatus(InternshipStatus.FILLED);
                internship.setVisibility(false);
            }
            if (internship.isFull()) {
                internship.setStatus(InternshipStatus.FILLED);
                internship.setVisibility(false);
            }
        }
    }

    /**
     * Applies a filter to the internship list.
     *
     * @param criteria a filtering object containing optional constraints
     * @return sorted list of internships matching the criteria
     */
    public List<Internship> filter(FilterCriteria criteria) {
        refreshStatuses();
        List<Internship> working = new ArrayList<>(internships);
        if (criteria != null) {
            working = working.stream()
                    .filter(criteria::matches)
                    .collect(Collectors.toList());
        }
        working.sort(Comparator.comparing(Internship::getTitle, String.CASE_INSENSITIVE_ORDER));
        return working;
    }

    /**
     * Returns all internships, automatically refreshing their statuses first.
     *
     * @return unmodifiable list of internships
     */
    public List<Internship> getInternships() {
        refreshStatuses();
        return Collections.unmodifiableList(internships);
    }

    /**
     * Returns all internships belonging to a specific company representative.
     *
     * @param rep the representative
     * @return unmodifiable list of their internships, or empty list if rep is null
     */
    public List<Internship> getInternshipsForRep(CompanyRep rep) {
        if (rep == null) {
            return Collections.emptyList();
        }
        refreshStatuses();
        List<Internship> result = internships.stream()
                .filter(internship -> internship.getRepInCharge() == rep)
                .collect(Collectors.toList());
        return Collections.unmodifiableList(result);
    }

    /**
     * Removes internships belonging to a representative, if found in the system.
     *
     * <p>This also removes them from the representative's own list via
     * {@link CompanyRep#removeInternship(Internship)}.</p>
     *
     * @param rep     the representative
     * @param targets the internships to remove
     */
    public void removeInternships(CompanyRep rep, Collection<Internship> targets) {
        if (rep == null || targets == null || targets.isEmpty()) {
            return;
        }
        List<Internship> removed = new ArrayList<>();
        internships.removeIf(internship -> {
            boolean match = internship.getRepInCharge() == rep && targets.contains(internship);
            if (match) {
                removed.add(internship);
            }
            return match;
        });
        for (Internship internship : removed) {
            rep.removeInternship(internship);
        }
    }
}
