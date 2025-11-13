package control;

import entity.CareerCenterStaff;
import entity.CompanyRep;
import entity.Student;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UserDataLoader {
    private static final String STUDENT_CSV = "data/sample_student_list.csv";
    private static final String STAFF_CSV = "data/sample_staff_list.csv";
    private static final String COMPANY_REP_CSV = "data/sample_company_representative_list.csv";

    public List<Student> loadStudents() {
        return readStudents(Paths.get(STUDENT_CSV));
    }

    public List<CareerCenterStaff> loadStaff() {
        return readStaff(Paths.get(STAFF_CSV));
    }

    public List<CompanyRep> loadApprovedCompanyReps() {
        return readCompanyReps(Paths.get(COMPANY_REP_CSV));
    }

    private List<Student> readStudents(Path path) {
        List<Student> result = new ArrayList<>();
        if (!Files.exists(path)) return result;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] c = line.split(",", -1);
                if (c.length < 5) continue;
                Student student = new Student(
                        c[0].trim(),
                        c[1].trim(),
                        "",
                        Integer.parseInt(c[3].trim()),
                        c[2].trim()
                );
                result.add(student);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load students", e);
        }
        return result;
    }

    private List<CareerCenterStaff> readStaff(Path path) {
        List<CareerCenterStaff> result = new ArrayList<>();
        if (!Files.exists(path)) return result;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] c = line.split(",", -1);
                if (c.length < 5) continue;
                CareerCenterStaff staff = new CareerCenterStaff(
                        c[4].trim(),
                        c[1].trim(),
                        ""
                );
                result.add(staff);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load staff", e);
        }
        return result;
    }

    private List<CompanyRep> readCompanyReps(Path path) {
        List<CompanyRep> result = new ArrayList<>();
        if (!Files.exists(path)) return result;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] c = line.split(",", -1);
                if (c.length < 7) continue;
                if (!"true".equalsIgnoreCase(c[6]) && !"approved".equalsIgnoreCase(c[6])) continue;
                CompanyRep rep = new CompanyRep(c[5].trim(), c[1].trim(), "");
                rep.setAuthorised(true);
                result.add(rep);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load company representatives", e);
        }
        return result;
    }
}
