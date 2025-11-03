import java.util.ArrayList;

public class Report {
    private String filterCriteria;
    private ArrayList<Internship> resultList = new ArrayList<>();

    public String getFilterCriteria() { return filterCriteria; }
    public void setFilterCriteria(String filterCriteria) { this.filterCriteria = filterCriteria; }

    public ArrayList<Internship> getResultList() { return resultList; }
    public void setResultList(ArrayList<Internship> resultList) { this.resultList = resultList; }

    // functions
    public void display() { }
}

