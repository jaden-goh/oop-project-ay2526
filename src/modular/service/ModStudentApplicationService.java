package modular.service;

import modular.model.ModApplication;
import modular.model.ModApplicationStatus;
import modular.model.ModInternship;
import modular.model.ModStudent;

import java.time.LocalDate;
import java.util.UUID;

public class ModStudentApplicationService {

    public boolean apply(ModStudent student, ModInternship internship) {
        if (student == null || internship == null) return false;
        long active = student.getApplications().stream()
                .map(ModApplication::getStatus)
                .filter(st -> st == ModApplicationStatus.PENDING || st == ModApplicationStatus.SUCCESSFUL)
                .count();
        if (active >= 3) {
            System.out.println("Max active applications reached.");
            return false;
        }
        if (student.getApplications().stream().anyMatch(ModApplication::isAccepted)) {
            System.out.println("You have already accepted an offer.");
            return false;
        }
        String id = "MAPP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ModApplication application = new ModApplication(id, student, internship, LocalDate.now());
        student.addApplication(application);
        return true;
    }
}
