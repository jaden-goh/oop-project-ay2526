package entity;

import java.util.ArrayList;
import java.util.function.Predicate;

public class Report {
    private Predicate<Internship> filterCriteria;
    private ArrayList<Internship> resultList = new ArrayList<>();

    public Predicate<Internship> getFilterCriteria() { return filterCriteria; }
    public void setFilterCriteria(Predicate<Internship> filterCriteria) { this.filterCriteria = filterCriteria; }

    public ArrayList<Internship> getResultList() { return resultList; }
    public void setResultList(ArrayList<Internship> resultList) { this.resultList = resultList; }

    // functions
    public void display() {
        if (resultList == null || resultList.isEmpty()) {
            System.out.println("No internships found for the selected criteria.");
            return;
        }
        System.out.println("===== Internship Report =====");
        for (Internship i : resultList) {
            System.out.println("Title: " + i.getTitle());
            System.out.println("Description: " + i.getDescription());
            System.out.println("Level: " + i.getLevel());
            System.out.println("Preferred Major: " + i.getPreferredMajor());
            System.out.println("Company: " + (i.getCompany() != null ? i.getCompany().getCompanyName() : "N/A"));
            System.out.println("Representative: " + (i.getRep() != null ? i.getRep().getName() : "N/A"));
            System.out.println("Slots: " + i.getNumSlots());
            System.out.println("Approved: " + (i.isApproved() ? "Yes" : "No"));
            System.out.println("Visible: " + (i.isVisibility() ? "Yes" : "No"));
            System.out.println("Open Date: " + i.getOpenDate());
            System.out.println("Close Date: " + i.getCloseDate());
            System.out.println("-----------------------------");
        }
    }
    public void display(ArrayList<Internship> allInternships) {
    if (filterCriteria == null) {
        System.out.println("No filter criteria specified.");
        return;
    }

    resultList.clear();
    for (Internship i : allInternships) {
        if (filterCriteria.test(i)) {
            resultList.add(i);
        }
    }

}

}

