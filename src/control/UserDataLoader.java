package control;

import entity.CareerCenterStaff;
import entity.Company;
import entity.CompanyRep;
import entity.Student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserDataLoader {
    private static final String STUDENT_CSV = "data/sample_student_list.csv";
    private static final String STAFF_CSV = "data/sample_staff_list.csv";
    private static final String COMPANY_REP_CSV = "data/sample_company_representative_list.csv";

    public List<Student> loadStudents() {
        return readCsv(STUDENT_CSV, 5).stream()
                .map(c -> new Student(c[0].trim(), c[1].trim(), "", c[3].trim(), c[2].trim()))
                .collect(Collectors.toList());
    }

    public List<CareerCenterStaff> loadStaff() {
        return readCsv(STAFF_CSV, 5).stream()
                .map(c -> new CareerCenterStaff(c[0].trim(),c[1].trim(),"password",c[2].trim(),c[3].trim(),c[4].trim()))
                .collect(Collectors.toList());
    }

    public List<CompanyRep> loadCompanyReps() {
        return readCsv(COMPANY_REP_CSV, 7).stream()
                .map(this::buildRep)
                .collect(Collectors.toList());
    }

    private List<String[]> readCsv(String file, int minCols) {
        Path path = Paths.get(file);
        if (!Files.exists(path)) return new ArrayList<>();
        try (Stream<String> lines = Files.lines(path)) {
            return lines.skip(1)
                    .map(l -> l.split(",", -1))
                    .filter(c -> c.length >= minCols)
                    .collect(Collectors.toList());
        } catch (IOException e) { throw new RuntimeException("Failed to load data from " + file, e); }
    }

    private CompanyRep buildRep(String[] c) {
        String email = c[5].trim();
        String companyName = c[2].trim();
        CompanyRep rep = new CompanyRep(email, c[1].trim(), "");
        rep.setEmail(email);
        rep.setDepartment(c[3].trim());
        rep.setPosition(c[4].trim());
        rep.setAuthorised(isApproved(c[6]));
        Company company = new Company();
        company.setCompanyName(companyName);
        rep.setCompany(company);
        return rep;
    }

    private boolean isApproved(String status) {
        return "true".equalsIgnoreCase(status) || "approved".equalsIgnoreCase(status);
    }
}
