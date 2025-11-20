// documented

package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an internship opportunity created by a {@link CompanyRep}.
 *
 * <p>An internship contains descriptive details, eligibility requirements,
 * application dates, visibility state, status, slot capacity, and a list
 * of student applications.</p>
 *
 * <p>Key behaviors include:</p>
 * <ul>
 *     <li>Storing internship details (title, description, major requirements)</li>
 *     <li>Managing visibility and status</li>
 *     <li>Accepting applications</li>
 *     <li>Holding a fixed number of internship slots</li>
 *     <li>Determining whether the internship has been fully filled</li>
 * </ul>
 */

public class Internship {

    /** Maximum number of internship slots allowed. */
    private static final int MAX_SLOTS = 10;

    /** Internship title. */
    private final String title;

    /** Internship description. */
    private final String description;

    /** Internship difficulty level (Basic, Intermediate, Advanced). */
    private InternshipLevel level;

    /** Preferred major required for application. */
    private String preferredMajor;

    /** Opening date for applications. */
    private LocalDate openDate;

    /** Closing date for applications. */
    private LocalDate closeDate;

    /** Whether this internship is currently visible to students. */
    private boolean visibility = true;

    /** Current approval/filled status of the internship. */
    private InternshipStatus status = InternshipStatus.PENDING;

    /** Name of the company offering the internship. */
    private final String companyName;

    /** Company representative responsible for this posting. */
    private final CompanyRep repInCharge;

    /** Available internship slots. */
    private final List<InternshipSlot> slots = new ArrayList<>();

    /** Applications submitted for this internship. */
    private final List<Application> applications = new ArrayList<>();

    /**
     * Constructs a new internship with basic details.
     *
     * @param title        internship title
     * @param description  internship description
     * @param companyName  name of the offering company
     * @param repInCharge  responsible company representative
     */
    public Internship(String title, String description, String companyName, CompanyRep repInCharge) {
        this.title = title;
        this.description = description;
        this.companyName = companyName;
        this.repInCharge = repInCharge;
    }

    /**
     * Returns the internship title.
     *
     * @return internship title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the internship description.
     *
     * @return internship description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the internship level.
     *
     * @return internship level
     */
    public InternshipLevel getLevel() {
        return level;
    }

    /**
     * Sets the internship level.
     *
     * @param level desired level
     */
    public void setLevel(InternshipLevel level) {
        this.level = level;
    }
    /**
     * Returns the preferred major filter.
     *
     * @return required major, or null if not restricted
     */
    public String getPreferredMajor() {
        return preferredMajor;
    }

    /**
     * Sets the preferred major. Blank or null values remove the restriction.
     *
     * @param preferredMajor required major name
     */
    public void setPreferredMajor(String preferredMajor) {
        this.preferredMajor = preferredMajor == null || preferredMajor.isBlank()
                ? null
                : preferredMajor.trim();
    }

    /**
     * Checks whether a given major is acceptable for this internship.
     *
     * @param major the student's major
     * @return true if the internship accepts the major
     */
    public boolean acceptsMajor(String major) {
        if (preferredMajor == null || preferredMajor.isBlank()) {
            return true;
        }
        if (major == null || major.isBlank()) {
            return false;
        }
        return preferredMajor.equalsIgnoreCase(major.trim());
    }

    /**
     * Returns the opening date for applications.
     *
     * @return application opening date
     */
    public LocalDate getOpenDate() {
        return openDate;
    }

    /**
     * Sets the application opening date.
     *
     * @param openDate start date
     */
    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    /**
     * Returns the closing date for applications.
     *
     * @return application closing date
     */
    public LocalDate getCloseDate() {
        return closeDate;
    }

    /**
     * Sets the closing date for applications.
     *
     * @param closeDate end date
     */
    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    /**
     * Returns whether the internship is visible to students.
     *
     * @return true if visible
     */
    public boolean isVisible() {
        return visibility;
    }

    /**
     * Sets the visibility of the internship.
     *
     * @param visibility true to show to students
     */
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    /**
     * Returns the internship status.
     *
     * @return internship status
     */
    public InternshipStatus getStatus() {
        return status;
    }

    /**
     * Sets the internship status.
     *
     * @param status new status
     */
    public void setStatus(InternshipStatus status) {
        this.status = status;
    }

    /**
     * Returns the name of the company offering this internship.
     *
     * @return company name
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Returns the representative who created and manages this internship.
     *
     * @return responsible company representative
     */
    public CompanyRep getRepInCharge() {
        return repInCharge;
    }

    /**
     * Adds a slot to this internship.
     *
     * @param slot the new slot
     *
     * @throws IllegalStateException if max slot capacity is reached
     */
    public void addSlot(InternshipSlot slot) {
        if (slot == null) {
            return;
        }
        if (slots.size() >= MAX_SLOTS) {
            throw new IllegalStateException("Cannot add more than " + MAX_SLOTS + " slots.");
        }
        slots.add(slot);
    }

    /**
     * Returns an unmodifiable list of slots.
     *
     * @return slot list
     */
    public List<InternshipSlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    /**
     * Returns an unmodifiable list of applications.
     *
     * @return application list
     */
    public List<Application> getApplications() {
        return Collections.unmodifiableList(applications);
    }

    /**
     * Adds an application to this internship.
     *
     * @param application the application to add
     */
    public void addApplication(Application application) {
        if (application != null) {
            applications.add(application);
        }
    }

    /**
     * Toggles visibility of this internship.
     *
     * @param on true to make visible
     */
    public void toggleVisibility(boolean on) {
        this.visibility = on;
    }

    /**
     * Checks whether all internship slots have been filled by students.
     *
     * @return true if every slot has an assigned student
     */
    public boolean isFull() {
        if (slots.isEmpty()) {
            return false;
        }
        for (InternshipSlot slot : slots) {
            if (slot.getAssignedStudent() == null) {
                return false;
            }
        }
        return true;
    }
}
