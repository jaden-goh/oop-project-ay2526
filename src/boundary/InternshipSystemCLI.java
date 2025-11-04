package boundary;
import control.InternshipManager;
import entity.*;
import java.util.ArrayList;

public class InternshipSystemCLI {
    private ArrayList<User> users = new ArrayList<>();
    private User currentUser;
    private InternshipManager manager = new InternshipManager();
    private ArrayList<Internship> internships = new ArrayList<>();

    public ArrayList<User> getUsers() { return users; }
    public void setUsers(ArrayList<User> users) { this.users = users; }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }

    public InternshipManager getManager() { return manager; }
    public void setManager(InternshipManager manager) { this.manager = manager; }

    public ArrayList<Internship> getInternships() { return internships; }
    public void setInternships(ArrayList<Internship> internships) { this.internships = internships; }

    // functions
    public void run() { }
    public void displayLoginMenu() { }
    public void displayStudentMenu() { }
    public void displayRepMenu() { }
    public void displayStaffMenu() { }

    public boolean register() { return false; }
    public Internship createInternship(String title, String description, String level, String preferredMajor,
                                       java.util.Date openDate, java.util.Date closeDate, int numSlots) { return null; }
    public void toggleVisibility(Internship internship) { }
    public boolean approveApplication(Application application) { return false; }
    public boolean rejectApplication(Application application) { return false; }
    public java.util.List<Application> viewApplications(Internship internship) { return null; }

    


    public static void main(String[] args){
        
    }
}

