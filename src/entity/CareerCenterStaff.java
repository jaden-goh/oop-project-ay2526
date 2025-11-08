package entity;
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
        if (approve){
            internship.setStatus(true);
            internship.setVisible(true);
            return true;
        }
        else {
            internship.setStatus(false);
            internship.setVisible(false);
            return false;
        }
    }
    public boolean approveWithdrawal(Application application, boolean approve) {
        if (approve){
            application.setStatus("Withdrawn");
            return true;
        }
        else {
            application.setRequestWithdrawal(false);
            return false;
        }
    }

    //Generates a report of internships filtered by status, major, level, and company.

    public void generateReport(List<Internship> internships, String filterStatus, String filterMajor, String filterLevel, String filterCompany) {
        System.out.println("\n--- Internship Report ---");
        for (Internship i : internships) {
            boolean matchesStatus = filterStatus.equals("All") || i.getStatus().toString().equalsIgnoreCase(filterStatus);
            boolean matchesMajor = filterMajor.equals("All") || i.getRequiredMajor().equalsIgnoreCase(filterMajor);
            boolean matchesLevel = filterLevel.equals("All") || i.getLevel().equalsIgnoreCase(filterLevel);
            boolean matchesCompany = filterCompany.equals("All") || i.getCompany().getName().equalsIgnoreCase(filterCompany);

            if (matchesStatus && matchesMajor && matchesLevel && matchesCompany) {
                System.out.println(i);
            }
        }
    }
}


