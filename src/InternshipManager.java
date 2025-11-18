import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InternshipManager {
    private static final int MAX_INTERNSHIPS_PER_REP = 5;
    private final List<Internship> internships = new ArrayList<>();

    public Internship submitInternship(Internship internship) {
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
        return internship;
    }

    public void approveInternship(Internship internship) {
        if (internship == null) {
            return;
        }
        internship.setStatus(InternshipStatus.APPROVED);
        internship.setVisibility(true);
    }

    public void rejectInternship(Internship internship) {
        if (internship == null) {
            return;
        }
        internship.setStatus(InternshipStatus.REJECTED);
        internship.setVisibility(false);
    }

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

    public List<Internship> getInternships() {
        refreshStatuses();
        return Collections.unmodifiableList(internships);
    }

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
}
