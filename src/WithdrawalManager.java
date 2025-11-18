import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WithdrawalManager {
    private final List<WithdrawalRequest> requests = new ArrayList<>();

    public WithdrawalRequest submitRequest(Application app, String reason) {
        if (app == null) {
            throw new IllegalArgumentException("Application required");
        }
        if (app.isWithdrawalRequested()) {
            throw new IllegalStateException("Withdrawal already requested.");
        }
        String trimmedReason = reason == null ? "" : reason.trim();
        WithdrawalRequest request = app.requestWithdrawal(trimmedReason);
        requests.add(request);
        return request;
    }

    public boolean processRequest(WithdrawalRequest request, CareerCenterStaff staff, boolean approve) {
        if (request == null || staff == null) {
            return false;
        }
        request.setProcessedBy(staff);
        if (approve) {
            request.approve();
            releaseSlot(request.getApplication());
        } else {
            request.reject();
        }
        requests.remove(request);
        return true;
    }

    public List<WithdrawalRequest> getPendingRequests() {
        return Collections.unmodifiableList(requests);
    }

    private void releaseSlot(Application application) {
        Internship internship = application.getInternship();
        for (InternshipSlot slot : internship.getSlots()) {
            if (slot.getAssignedStudent() == application.getStudent()) {
                slot.release();
                internship.setStatus(InternshipStatus.APPROVED);
                internship.toggleVisibility(true);
                break;
            }
        }
    }
}
