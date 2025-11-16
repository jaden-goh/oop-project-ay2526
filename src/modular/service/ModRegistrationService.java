package modular.service;

import modular.model.ModCareerCenterStaff;
import modular.model.ModCompany;
import modular.model.ModCompanyRep;
import modular.model.ModStudent;
import modular.model.ModUser;
import modular.repository.ModCareerStaffRepository;
import modular.repository.ModCompanyRepRepository;
import modular.repository.ModStudentRepository;
import modular.service.dto.ModCompanyRepRegistrationData;
import modular.service.dto.ModStaffRegistrationData;
import modular.service.dto.ModStudentRegistrationData;

import java.io.IOException;
import java.util.Collection;

public class ModRegistrationService {
    private final ModStudentRepository studentRepository;
    private final ModCompanyRepRepository companyRepRepository;
    private final ModCareerStaffRepository staffRepository;
    private final Collection<ModUser> users;

    public ModRegistrationService(ModStudentRepository studentRepository,
                                  ModCompanyRepRepository companyRepRepository,
                                  ModCareerStaffRepository staffRepository,
                                  Collection<ModUser> users) {
        this.studentRepository = studentRepository;
        this.companyRepRepository = companyRepRepository;
        this.staffRepository = staffRepository;
        this.users = users;
    }

    public ModStudent registerStudent(ModStudentRegistrationData data) {
        if (studentRepository.existsById(data.getStudentId())) {
            System.out.println("Student already exists.");
            return null;
        }
        ModStudent student = new ModStudent(data.getStudentId(), data.getName(), "password", data.getYear(), data.getMajor());
        student.setEmail(data.getEmail());
        try {
            studentRepository.saveRecord(student);
            users.add(student);
            System.out.println("Registration completed! Welcome, " + student.getName());
        } catch (IOException e) {
            System.out.println("Failed to save student: " + e.getMessage());
            return null;
        }
        return student;
    }

    public ModCompanyRep registerRep(ModCompanyRepRegistrationData data) {
        if (companyRepRepository.existsById(data.getRepId())) {
            System.out.println("Representative already exists.");
            return null;
        }
        ModCompanyRep rep = new ModCompanyRep(data.getRepId(), data.getName(), "password");
        rep.setDepartment(data.getDepartment());
        rep.setPosition(data.getPosition());
        rep.setEmail(data.getEmail());
        rep.setAuthorised(false);
        ModCompany company = new ModCompany(data.getCompanyName());
        rep.setCompany(company);
        try {
            companyRepRepository.saveRecord(rep);
            users.add(rep);
            System.out.println("Registration submitted. Await approval.");
        } catch (IOException e) {
            System.out.println("Failed to save rep: " + e.getMessage());
            return null;
        }
        return rep;
    }

    public ModCareerCenterStaff registerStaff(ModStaffRegistrationData data) {
        if (staffRepository.existsById(data.getStaffId())) {
            System.out.println("Staff already exists.");
            return null;
        }
        ModCareerCenterStaff staff = new ModCareerCenterStaff(data.getStaffId(), data.getName(), "password", data.getRole(), data.getDepartment());
        staff.setEmail(data.getEmail());
        try {
            staffRepository.saveRecord(staff);
            users.add(staff);
            System.out.println("Staff registration completed.");
        } catch (IOException e) {
            System.out.println("Failed to save staff: " + e.getMessage());
            return null;
        }
        return staff;
    }
}
