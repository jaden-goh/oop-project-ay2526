public class InternshipSlot {
    private final int slotNumber;
    private Student assignedStudent;

    public InternshipSlot(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public Student getAssignedStudent() {
        return assignedStudent;
    }

    public void assignStudent(Student student) {
        if (assignedStudent != null) {
            throw new IllegalStateException("Slot already assigned.");
        }
        this.assignedStudent = student;
    }

    public void release() {
        this.assignedStudent = null;
    }
}
