// documented

package entity;

/**
 * Represents a single internship slot within an {@link Internship}.
 *
 * <p>Each slot can be assigned to exactly one student. Slots help determine
 * whether an internship is full and allow tracking of which student has secured
 * which position.</p>
 */

public class InternshipSlot {

    /** Unique slot number within the internship (1-based index). */
    private final int slotNumber;

    /** The student currently assigned to this slot, or null if unassigned. */
    private Student assignedStudent;

    /**
     * Creates a new internship slot with the given slot number.
     *
     * @param slotNumber sequential identifier for the slot
     */
    public InternshipSlot(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    /**
     * Returns the numeric identifier of this slot.
     *
     * @return slot number
     */
    public int getSlotNumber() {
        return slotNumber;
    }

    /**
     * Returns the student assigned to this slot.
     *
     * @return assigned student, or null if unassigned
     */
    public Student getAssignedStudent() {
        return assignedStudent;
    }

    /**
     * Assigns the slot to the given student.
     *
     * @param student the student to assign
     *
     * @throws IllegalStateException if the slot has already been assigned
     */
    public void assignStudent(Student student) {
        if (assignedStudent != null) {
            throw new IllegalStateException("Slot already assigned.");
        }
        this.assignedStudent = student;
    }

    /**
     * Releases the student assigned to this slot, making the slot available again.
     */
    public void release() {
        this.assignedStudent = null;
    }
}
