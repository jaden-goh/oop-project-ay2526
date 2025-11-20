// documented

package entity;

import control.InternshipManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Represents a Company Representative user in the system.
 *
 * <p>A Company Representative is responsible for creating and managing internship
 * opportunities for their company. They must first be approved by a
 * {@link CareerCenterStaff} before they can create or modify internships.</p>
 *
 * <p>Capabilities include:</p>
 * <ul>
 *     <li>Creating internship opportunities (up to 5)</li>
 *     <li>Toggling visibility of their internships</li>
 *     <li>Viewing applications submitted to their internships</li>
 *     <li>Managing internship slots</li>
 * </ul>
 */
public class CompanyRep extends User {

    /** Maximum number of internship postings a representative may create. */
    private static final int MAX_INTERNSHIPS = 5;

    /** Maximum number of slots allowed per internship. */
    private static final int MAX_SLOTS = 10;

    /** Name of the company this representative belongs to. */
    private String companyName;

    /** Department of the company representative. */
    private String department;

    /** Position held by the representative in the company. */
    private String position;

    /** Internships created by this representative. */
    private final List<Internship> internships = new ArrayList<>();

    /** Whether this representative has been approved by Career Center Staff. */
    private boolean approved;

    /**
     * Creates a new Company Representative.
     *
     * @param userID        the representative's user ID
     * @param name          the representative's name
     * @param password      password for login
     * @param companyName   the name of the representative's company
     * @param department    the department within the company
     * @param position      the representative's position
     * @param approved      whether the account has been approved
     */
    public CompanyRep(String userID, String name, String password,
                      String companyName, String department, String position,
                      boolean approved) {
        super(userID, name, password);
        this.companyName = companyName;
        this.department = department;
        this.position = position;
        this.approved = approved;
    }

    /**
     * Returns the company name of the representative.
     *
     * @return the company name
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Updates the company name of the representative.
     *
     * @param companyName the new company name
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * Returns the department of the representative.
     *
     * @return the department name
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Returns the job position of the representative.
     *
     * @return the job position
     */
    public String getPosition() {
        return position;
    }

    /**
     * Creates and submits a new internship opportunity managed by this representative.
     *
     * <p>This method enforces:</p>
     * <ul>
     *     <li>The representative must be approved</li>
     *     <li>A maximum of 5 internships</li>
     *     <li>Valid slot count (1–10)</li>
     *     <li>Valid date range</li>
     * </ul>
     *
     * @param manager        the internship manager to handle submission
     * @param title          internship title
     * @param description    internship description
     * @param level          internship level (Basic, Intermediate, Advanced)
     * @param preferredMajor preferred major requirement
     * @param openDate       application opening date
     * @param closeDate      application closing date
     * @param slotCount      number of available slots
     *
     * @return the created {@link Internship} instance
     *
     * @throws IllegalStateException    if representative is not approved or max internships reached
     * @throws IllegalArgumentException if manager is null, slot count invalid, or dates invalid
     */

    public Internship createInternship(InternshipManager manager, String title, String description,
                                       InternshipLevel level, String preferredMajor,
                                       LocalDate openDate, LocalDate closeDate,
                                       int slotCount) {
        ensureApproved();
        if (manager == null) {
            throw new IllegalArgumentException("Internship manager required.");
        }
        if (internships.size() >= MAX_INTERNSHIPS) {
            throw new IllegalStateException("Maximum number of internships (" + MAX_INTERNSHIPS + ") reached.");
        }
        if (slotCount < 1 || slotCount > MAX_SLOTS) {
            throw new IllegalArgumentException("Slot count must be between 1 and " + MAX_SLOTS);
        }
        if (closeDate != null && closeDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Closing date cannot be in the past.");
        }
        if (openDate != null && closeDate != null && closeDate.isBefore(openDate)) {
            throw new IllegalArgumentException("Closing date cannot be earlier than the opening date.");
        }
        Internship internship = new Internship(title, description, companyName, this);
        internship.setLevel(level);
        internship.setPreferredMajor(preferredMajor);
        internship.setOpenDate(openDate);
        internship.setCloseDate(closeDate);
        for (int i = 1; i <= slotCount; i++) {
            internship.addSlot(new InternshipSlot(i));
        }
        manager.submitInternship(internship);
        internships.add(internship);
        return internship;
    }

    /**
     * Returns a read-only list of applications submitted for the given internship.
     * The internship must belong to this representative.
     *
     * @param manager    the internship manager providing application records
     * @param internship the internship to view applications for
     * @return unmodifiable list of applications, or an empty list if invalid
     */

    public List<Application> viewApplications(InternshipManager manager, Internship internship) {
        if (manager == null) {
            throw new IllegalArgumentException("Internship manager required.");
        }
        if (internship == null || !internships.contains(internship)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(internship.getApplications());
    }

    /**
     * Toggles visibility of a managed internship.
     *
     * @param manager    the internship manager handling visibility logic
     * @param internship the internship to toggle
     * @param on         {@code true} to make visible, {@code false} to hide
     *
     * @throws IllegalStateException    if representative is not approved
     * @throws IllegalArgumentException if manager is null or internship not owned by this rep
     */

    public void toggleVisibility(InternshipManager manager, Internship internship, boolean on) {
        ensureApproved();
        if (manager == null) {
            throw new IllegalArgumentException("Internship manager required.");
        }
        if (internship == null || !internships.contains(internship)) {
            throw new IllegalArgumentException("Internship not managed by this representative.");
        }
        internship.toggleVisibility(on);
        if (on && internship.getStatus() == InternshipStatus.PENDING) {
            manager.approveInternship(internship);
        }
    }

    /**
     * Removes an internship from this representative's list.
     *
     * @param internship the internship to remove
     */
    public void removeInternship(Internship internship) {
        internships.remove(internship);
    }

    /**
     * Returns the list of internships created by this representative.
     * If a manager is provided, uses the manager to fetch the official list.
     *
     * @param manager optional internship manager
     * @return unmodifiable list of managed internships
     */
    public List<Internship> getInternships(InternshipManager manager) {
        if (manager != null) {
            return manager.getInternshipsForRep(this);
        }
        return Collections.unmodifiableList(internships);
    }

    /**
     * Returns whether this representative has been approved by Career Center Staff.
     *
     * @return true if approved, false otherwise
     */
    public boolean isApproved() {
        return approved;
    }

    /**
     * Sets whether this representative has been approved by Career Center Staff.
     *
     * @param approved approval status
     */
    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    /**
     * Ensures the representative is approved before performing restricted actions.
     *
     * @throws IllegalStateException if not approved
     */
    private void ensureApproved() {
        if (!approved) {
            throw new IllegalStateException("Company representative has not been approved yet.");
        }
    }
}
