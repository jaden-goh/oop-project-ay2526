package entity;
import java.util.ArrayList;

public class CompanyRep extends User {
    private Company company; // owning company
    private String name;
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

    // functions

    // for careercenterstaff to approve (this just indicates that the rep has submitted the registration)
    public boolean register() { return false; }

    // create an internship posting by this rep
    // status: pending, visibility: false (students are unable to see this until it is approved)
    public Internship createInternship(String title, String description, String level, String preferredMajor,
                                       java.util.Date openDate, java.util.Date closeDate, int numSlots) 
                                       
                                       {
                                        Internship i = new Internship();

                                        i.setTitle(title);
                                        i.setDescription(description);
                                        i.setLevel(level);
                                        i.setPreferredMajor(preferredMajor);
                                        i.setOpenDate(openDate);
                                        i.setCloseDate(closeDate);
                                    
                                        // hidden from students first
                                        i.setVisibility(false); 

                                        // not yet approved by career staff
                                        i.setApproved(false);               
                                        i.updateStatus(InternshipStatus.PENDING);

                                        // link the internship to the rep's company
                                        i.setCompany(company);              
                                    
                                        // create the number of empty slots
                                        ArrayList<InternshipSlot> slots = new ArrayList<>();
                                        for (int s = 1; s <= numSlots; s++) {
                                            InternshipSlot slot = new InternshipSlot(s);
                                            slot.setSlotID(s);
                                            slot.setApplication(null);
                                            slots.add(slot);
                                        }

                                        i.setSlots(slots);
                                    
                                        // link this internship to the rep and company
                                        internships.add(i);
                                        company.addInternship(i);
                                    
                                        return i;
                                       }


    // visibility allowed only for approved postings
    public void toggleVisibility(Internship internship) 
    {         
        // if the internship itself is null nothing to toggle
        if (internship == null) 
            { return; }

        if (internship.getStatus() != InternshipStatus.APPROVED) {
            throw new IllegalStateException("Can only toggle visibility for 'Approved' internships.");
        }

        internship.setVisibility(!internship.isVisibility());

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
        for (InternshipSlot slot: internship.getSlots())  
        {
            if (!slot.isFilled()) 
            {
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

}


