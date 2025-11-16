package modular.model;

public class ModInternshipSlot {
    private int slotId;
    private boolean filled;
    private ModApplication application;

    public ModInternshipSlot(int slotId) {
        this.slotId = slotId;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public boolean isFilled() {
        return filled;
    }

    public ModApplication getApplication() {
        return application;
    }

    public void setApplication(ModApplication application) {
        this.application = application;
        this.filled = application != null;
    }

    public boolean assign(ModApplication application) {
        if (filled) return false;
        setApplication(application);
        return true;
    }
}
