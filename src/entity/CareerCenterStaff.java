package entity;
import java.util.List;
public class CareerCenterStaff extends User {
    private String id;
    private String name;
    private String role; 
    private String department;
    private String email;


    public CareerCenterStaff(String id, String name, String password, String role, String department, String email) {
        super(id, name, password);
        this.id = id;
        this.name = name;
        this.role = role;
        this.department = department;
        this.email = email;
    }

    public void setid(String id){this.id = id;}
    public String getid(){return id;}

    public void setname(String name){this.name = name;}
    public String getname(){return name;}

    public void setrole(String role){this.role = role;}
    public String getrole(){return role;}

    public void setdepartment(String department){this.department = department;}
    public String getdepartment(){return department;}

    public void setemail(String email){this.email = email;}
    public String getemail(){return email;}


    public boolean authoriseRep(CompanyRep rep, boolean approve) {
        if (rep == null) {
            System.out.println("No representative selected.");
            return false;
        }
        rep.setAuthorised(approve);
        System.out.println(rep.getName() + (approve ? " has been approved." : " has been rejected."));
        return approve;
    }

    public boolean approveInternship(Internship internship, boolean approve) {
        internship.updateStatus(approve ? InternshipStatus.APPROVED : InternshipStatus.REJECTED);
        internship.setApproved(approve);
        internship.setVisibility(approve);
        return approve;
    }
    public boolean approveWithdrawal(Application application, boolean approve) {
        if (approve){
            application.updateStatus(ApplicationStatus.WITHDRAWN);
            return true;
        }
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

    public void handleInternshipRequests(java.util.List<Internship> internships, java.util.Scanner scanner) {
        if (internships == null || internships.isEmpty()) {
            System.out.println("No internships to review.");
            return;
        }
        java.util.ArrayList<Internship> pending = new java.util.ArrayList<>();
        for (Internship internship : internships) {
            if (internship.getStatus() == InternshipStatus.PENDING) {
                pending.add(internship);
            }
        }
        if (pending.isEmpty()) {
            System.out.println("No pending internship requests.");
            return;
        }
        for (int i = 0; i < pending.size(); i++) {
            Internship in = pending.get(i);
            String companyName = in.getCompany() != null ? in.getCompany().getCompanyName() : "Unknown Company";
            System.out.printf("%d. %s (%s)%n", i + 1, in.getTitle(), companyName);
        }
        System.out.print("Select internship to review: ");
        int idx;
        try {
            idx = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (idx < 1 || idx > pending.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        System.out.print("Approve internship? (y/n): ");
        boolean approve = scanner.nextLine().trim().equalsIgnoreCase("y");
        approveInternship(pending.get(idx - 1), approve);
    }

    public void handleWithdrawalRequests(java.util.List<User> users, java.util.Scanner scanner) {
        java.util.ArrayList<Application> pending = new java.util.ArrayList<>();
        for (User user : users) 
            if (user instanceof Student s) 
                for (Application app : s.getApplications()) 
                    if (app.getStatus() == ApplicationStatus.PENDING_WITHDRAWAL) 
                        pending.add(app);
        if (pending.isEmpty()) {
            System.out.println("No withdrawal requests.");
            return;
        }
        for (int i = 0; i < pending.size(); i++) {
            Application app = pending.get(i);
            System.out.printf("%d. %s - %s%n", i + 1, app.getStudent().getName(), app.getInternship().getTitle());
        }
        System.out.print("Select request: ");
        int idx;
        try { idx = Integer.parseInt(scanner.nextLine().trim()); } catch (NumberFormatException e) { System.out.println("Invalid selection."); return; }
        if (idx < 1 || idx > pending.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        System.out.print("Approve withdrawal? (y/n): ");
        boolean approve = scanner.nextLine().trim().equalsIgnoreCase("y");
        Application target = pending.get(idx - 1);
        if (approve) {
            approveWithdrawal(target, true);
            target.setWithdrawn(true);
            target.updateStatus(ApplicationStatus.WITHDRAWN);
            System.out.println("Withdrawal approved.");
        } else {
            target.setWithdrawn(false);
            target.updateStatus(ApplicationStatus.PENDING);
            System.out.println("Withdrawal rejected.");
        }
    }

    public void displayDetails(){
        System.out.println("ID: " + getid());
        System.out.println("Name: " + getname());
        System.out.println("Role: " + getrole());
        System.out.println("Department: " + getdepartment());
        System.out.println("Email: " + getemail());
    }
}


