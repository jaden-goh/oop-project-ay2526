package entity;
import java.util.ArrayList;

public class CompanyRep extends User {
    private Company company; // owning company
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

    // 
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
                                            InternshipSlot slot = new InternshipSlot();
                                            slot.setSlotID(s);
                                            slot.setFilled(false);
                                            slot.setApplication(null);
                                            slots.add(slot);
                                        }
                                        i.setSlots(slots);
                                    
                                        // link this internship to the rep and company
                                        internships.add(i);
                                        company.addInternship(i);
                                    
                                        return i;
                                       }


    public void toggleVisibility(Internship internship) 
    {         
        if (internship == null) return;
        if (internship.getStatus() != InternshipStatus.APPROVED) {
            throw new IllegalStateException("Can only toggle visibility for 'Approved' internships.");
        }
        internship.setVisibility(!internship.isVisibility());

    }
    
    
    
    public boolean approveApplication(Application application) { return false; }
    public boolean rejectApplication(Application application) { return false; }
    public java.util.List<Application> viewApplications(Internship internship) { return null; }
}


