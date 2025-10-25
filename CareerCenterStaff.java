public class CareerCenterStaff extends User {
    public CareerCenterStaff(String id, String name, String password) {
        super(id, name, password);
    }
    public boolean authoriseRep() {return true;}
    
    public boolean approveInternship() {return true;}
    
    public void generateReport() {}
    
    public boolean approveWithdrawal() {return true;}
}


