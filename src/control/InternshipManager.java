package control;
import entity.*;

import java.util.ArrayList;
import java.util.List;

import entity.Internship;
import entity.Student;

public class InternshipManager {
    private ArrayList<Internship> internships = new ArrayList<>();

    public ArrayList<Internship> getInternships() { return internships; }
    public void setInternships(ArrayList<Internship> internships) { this.internships = internships; }

    // functions
    public List<Internship> filterByCriteria(Comparable<?> criteria) { return null; }
    public List<Internship> getVisibleInternships(Student student) { return null; }
    public List<Internship> getAllInternships() { return null; }
}

