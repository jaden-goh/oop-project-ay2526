package entity;
public class InternshipSlot {
    private int slotID;
    private String status;
    private Application application;

    public InternshipSlot() {}

    public int getSlotID() { return slotID; }
    public void setSlotID(int slotID) { this.slotID = slotID; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    // functions
    public boolean assignStudent(Application application) { return false; }
    public void markFilled() { }
    public boolean isFilled() { return false; }
}

