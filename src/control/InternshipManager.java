package control;

import entity.Internship;
import entity.InternshipStatus;
import entity.Student;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class InternshipManager {
    private ArrayList<Internship> internships = new ArrayList<>();

    public ArrayList<Internship> getInternships() {
        return internships;
    }

    public void setInternships(ArrayList<Internship> internships) {
        this.internships = internships != null ? internships : new ArrayList<>();
    }

    public List<Internship> filterBy(Predicate<Internship> criteria) {
        List<Internship> result = new ArrayList<>();
        for (Internship i : internships) {
            if (criteria.test(i)) {
                result.add(i);
            }
        }
        return result;
    }

    public List<Internship> getVisibleInternships(Student student) {
        return filterBy(i ->
            i.isVisibility() &&
            i.isApproved() &&
            i.getStatus() == InternshipStatus.APPROVED &&
                    i.getPreferredMajor() != null &&
                    i.getPreferredMajor().equalsIgnoreCase(student.getMajor()) &&
                    isEligibleByYearLevel(student, i)
        );
    }

    private boolean isEligibleByYearLevel(Student s, Internship i){
        String level = i.getLevel();
        if (level == null) return false;

        if (s.getYear() <= 2){
            return level.equalsIgnoreCase("Basic");
        }

        return level.equalsIgnoreCase("Basic")
                || level.equalsIgnoreCase("Intermediate")
                || level.equalsIgnoreCase("Advanced");
    }



    public List<Internship> getAllInternships() {
        return new ArrayList<>(internships);
    }
}
