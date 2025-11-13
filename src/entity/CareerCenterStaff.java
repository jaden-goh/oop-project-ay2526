package entity;
import java.util.List;
public class CareerCenterStaff extends User {
    public CareerCenterStaff(String id, String name, String password) {
        super(id, name, password);
    }

    public boolean authoriseRep(CompanyRep rep, boolean approve) {
        if (approve){
            rep.setAuthorised(true);
            return true;
        }
        else {
            rep.setAuthorised(false);
            return false;   
        }
    }

    public boolean approveInternship(Internship internship, boolean approve) {
        internship.updateStatus(approve ? InternshipStatus.APPROVED : InternshipStatus.REJECTED);
        internship.setApproved(approve);
        internship.setVisibility(approve);
        return approve;
    }
    public boolean approveWithdrawal(Application application, boolean approve) {
        if (approve && application.approveWithdrawal()){
            application.updateStatus(ApplicationStatus.WITHDRAWN);
            return true;
        }
        application.rejectWithdrawal();
        return false;
    }

    //Generates a report of internships filtered by status, major, level, and company.

    public void generateReport(List<Internship> internships, String filterStatus, String filterMajor, String filterLevel, String filterCompany) {
        System.out.println("\n--- Internship Report ---");
        for (Internship i : internships) {
            boolean matchesStatus = filterStatus.equals("All") || i.getStatus().toString().equalsIgnoreCase(filterStatus);
            boolean matchesMajor = filterMajor.equals("All") || i.getPreferredMajor().equalsIgnoreCase(filterMajor);
            boolean matchesLevel = filterLevel.equals("All") || i.getLevel().equalsIgnoreCase(filterLevel);
            boolean matchesCompany = filterCompany.equals("All") || i.getCompany().getCompanyName().equalsIgnoreCase(filterCompany);

            if (matchesStatus && matchesMajor && matchesLevel && matchesCompany) {
                System.out.println(i);
            }
        }
    }
}


