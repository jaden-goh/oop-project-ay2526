package entity;

public class InternshipSlot {

    private int slotID;
    private boolean filled; 
    private Application application;

    public InternshipSlot(int slotID) {
        this.slotID = slotID;
        this.filled = false;
        this.application = null;
    }

    public int getSlotID() {
        return slotID;
    }

    public void setSlotID(int slotID) {
        this.slotID = slotID;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
        this.filled = true;
    }

    public boolean assignStudent(Application application) {
        if (isFilled()) return false;
        this.application = application;
        this.filled = true;
        return true;
    }

    public void markFilled() {  // note: does not assign any student
        this.filled = true;
    }

    public boolean isFilled() {
        return this.filled;
    }
}
