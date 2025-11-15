package entity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class CompanyRep extends User {
    private Company company; // owning company
    private String department;
    private String position;
    private String email;
    private boolean authorised; // false until staff authorises
    private ArrayList<Internship> internships; // postings created by this rep

    public CompanyRep(String id, String name, String password) {
        super(id, name, password);
        this.authorised = false;
        this.internships = new ArrayList<>();
    }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public boolean isAuthorised() { return authorised; }
    public void setAuthorised(boolean authorised) { this.authorised = authorised; }

    public ArrayList<Internship> getInternships() { return internships; }
    public void setInternships(ArrayList<Internship> internships) { this.internships = internships; }

    public String getDepartment(){ return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition(){ return position; }
    public void setPosition(String position) { this.position = position; }

    public String getEmail(){ return email; }
    public void setEmail(String email) { this.email = email; }

    // functions

    // for careercenterstaff to approve (this just indicates that the rep has submitted the registration)
    public boolean register() { return false; }

    // create an internship posting by this rep
    // status: pending, visibility: false (students are unable to see this until it is approved)
    public Internship createInternship() {
        if (company == null) {
            System.out.println("You must be linked to a company before creating an internship.");
            return null;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Create Internship ===");
        String title = prompt(scanner, "Title");
        String description = prompt(scanner, "Description");
        String level = prompt(scanner, "Level (Basic/Intermediate/Advanced)");
        String preferredMajor = prompt(scanner, "Preferred Major");
        Date openDate = readDate(scanner, "Opening date (yyyy-MM-dd)");
        Date closeDate = readDate(scanner, "Closing date (yyyy-MM-dd)");
        if (openDate == null || closeDate == null) {
            System.out.println("Invalid dates entered. Internship creation aborted.");
            return null;
        }
        Integer slots = readInteger(scanner, "Number of slots");
        if (slots == null || slots <= 0) {
            System.out.println("Number of slots must be a positive integer.");
            return null;
        }

        System.out.print("Internship created.");
        return buildInternship(title, description, level, preferredMajor, openDate, closeDate, slots);
    }

    private String prompt(Scanner scanner, String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    private Date readDate(Scanner scanner, String label) {
        System.out.print(label + ": ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setLenient(false);
        try {
            return format.parse(input);
        } catch (ParseException e) {
            System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            return null;
        }
    }

    private Integer readInteger(Scanner scanner, String label) {
        System.out.print(label + ": ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return null;
        try {
            return Integer.valueOf(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return null;
        }
    }

    // visibility allowed only for approved postings
    public void toggleVisibility() {
        if (internships.isEmpty()) {
            System.out.println("No internships available.");
            return;
        }
        System.out.println("Select an internship to toggle visibility:");
        for (int i = 0; i < internships.size(); i++) {
            Internship in = internships.get(i);
            System.out.printf("%d. %s (Visible: %s, Status: %s)%n",
                    i + 1, in.getTitle(), in.isVisibility(), in.getStatus());
        }
        System.out.print("Choice: ");
        Scanner scanner = new Scanner(System.in);
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (choice < 1 || choice > internships.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        Internship internship = internships.get(choice - 1);
        if (internship.getStatus() != InternshipStatus.APPROVED) {
            System.out.println("Only approved internships can change visibility.");
            return;
        }
        internship.setVisibility(!internship.isVisibility());
        System.out.println("Visibility updated: " + internship.isVisibility());
    }

    public void manageApplications() {
        if (internships.isEmpty()) {
            System.out.println("No internships available.");
            return;
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select an internship to manage:");
        for (int i = 0; i < internships.size(); i++) {
            Internship in = internships.get(i);
            System.out.printf("%d. %s (%s)%n", i + 1, in.getTitle(), in.getStatus());
        }
        System.out.print("Choice: ");
        int internshipChoice;
        try {
            internshipChoice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (internshipChoice < 1 || internshipChoice > internships.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        java.util.List<Application> applications = viewApplications(internships.get(internshipChoice - 1));
        if (applications.isEmpty()) {
            System.out.println("No applications to manage for this internship.");
            return;
        }
        System.out.println("Select an application:");
        for (int i = 0; i < applications.size(); i++) {
            Application app = applications.get(i);
            System.out.printf("%d. %s (%s)%n", i + 1, app.getStudent().getName(), app.getStatus());
        }
        System.out.print("Choice: ");
        int appChoice;
        try {
            appChoice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (appChoice < 1 || appChoice > applications.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        Application selected = applications.get(appChoice - 1);
        System.out.print("Approve (A) or Reject (R): ");
        String decision = scanner.nextLine().trim().toUpperCase();
        if ("A".equals(decision)) {
            System.out.println(approveApplication(selected) ? "Application approved." : "Unable to approve application.");
        } else if ("R".equals(decision)) {
            System.out.println(rejectApplication(selected) ? "Application rejected." : "Unable to reject application.");
        } else {
            System.out.println("Invalid action.");
        }
    }
    
    
    // approving an application --> status: successful, mark one available slot filled
    // if all slots filled, internship status: filled 
    public boolean approveApplication(Application application) 
    { 
        // nothing to review, approve or reject 
        if (application == null) 
            { return false; }
        
        // application isn't linked to any internship 
        Internship internship = application.getInternship();
        if (internship == null) 
            { return false; } 
        
        // ensure that this posting belongs to this rep  
        if (!this.internships.contains(internship)) 
            { return false; }

        // if the internship is full, it can no longer be approved
        if (internship.isFull()) 
            { return false; }

        // mark application as successful 
        application.setStatus(ApplicationStatus.SUCCESSFUL);

        // fill the 1st free slot up 
        for (InternshipSlot slot: internship.getSlots()){
            if (!slot.isFilled()){
                slot.markFilled();
                slot.setApplication(application);
                break;
            }
        }
        // if all the internship slots are filled up, we mark the posting as filled 
        if (internship.isFull()) {
            internship.updateStatus(InternshipStatus.FILLED);
        }
        return true;
    }

    
    public boolean rejectApplication(Application application) {         
        if (application == null) 
            { return false; }       
        Internship internship = application.getInternship();
        if (internship == null) 
            { return false; } 
        if (!this.internships.contains(internship)) 
            { return false; }        
        application.setStatus(ApplicationStatus.UNSUCCESSFUL);
        return true;
    }

    // returns a list if the internship belongs to this rep 
    // otherwise an empty list is returned 
    public java.util.List<Application> viewApplications(Internship internship) { 
        
        if (internship == null) 
            { return java.util.Collections.emptyList(); }        
        if (!this.internships.contains(internship)) 
            { return java.util.Collections.emptyList(); }
        java.util.ArrayList<Application> apps = new java.util.ArrayList<>();
        for (InternshipSlot slot: internship.getSlots()) { 
            Application app = slot.getApplication();              
            if (app != null) 
                { apps.add(app); }
        }
        return apps;    
    }

    public void displayDetails(){
        System.out.println("ID: " + getId());
        System.out.println("Name: " + getName());
        System.out.println("Company: " + company.getCompanyName());
        System.out.println("Department: " + getDepartment());
        System.out.println("Email: " + getEmail());
    }

    private Internship buildInternship(String title, String description, String level, String preferredMajor,
                                       Date openDate, Date closeDate, int numSlots) {
        Internship internship = new Internship();
        internship.setTitle(title);
        internship.setDescription(description);
        internship.setLevel(level);
        internship.setPreferredMajor(preferredMajor);
        internship.setOpenDate(openDate);
        internship.setCloseDate(closeDate);
        internship.setVisibility(false);
        internship.setApproved(false);
        internship.updateStatus(InternshipStatus.PENDING);
        internship.setCompany(company);

        ArrayList<InternshipSlot> slots = new ArrayList<>();
        for (int s = 1; s <= numSlots; s++) {
            InternshipSlot slot = new InternshipSlot(s);
            slot.setSlotID(s);
            slot.setApplication(null);
            slots.add(slot);
        }
        internship.setSlots(slots);

        internships.add(internship);
        company.addInternship(internship);
        return internship;
    }

}


