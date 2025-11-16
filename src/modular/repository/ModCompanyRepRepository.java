package modular.repository;

import modular.model.ModCareerCenterStaff;
import modular.model.ModCompany;
import modular.model.ModCompanyRep;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModCompanyRepRepository {
    private final Path csvPath;

    public ModCompanyRepRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    public List<ModCompanyRep> loadAll() {
        List<ModCompanyRep> reps = new ArrayList<>();
        if (csvPath == null || !Files.exists(csvPath)) {
            return reps;
        }
        try (var reader = Files.newBufferedReader(csvPath)) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] columns = line.split(",", -1);
                if (columns.length < 7) continue;
                ModCompanyRep rep = new ModCompanyRep(columns[0], columns[1], "password");
                rep.setDepartment(columns[3]);
                rep.setPosition(columns[4]);
                rep.setEmail(columns[5]);
                rep.setAuthorised(Boolean.parseBoolean(columns[6]));
                ModCompany company = new ModCompany(columns[2]);
                rep.setCompany(company);
                company.addRep(rep);
                reps.add(rep);
            }
        } catch (IOException e) {
            System.out.println("Failed to load modular company representatives: " + e.getMessage());
        }
        return reps;
    }

    public ModCompanyRep findById(String id) {
        if (id == null || csvPath == null || !Files.exists(csvPath)) return null;
        try (var reader = Files.newBufferedReader(csvPath)) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] columns = line.split(",", -1);
                if (columns.length < 7) continue;
                if (columns[0].equalsIgnoreCase(id)) {
                    ModCompanyRep rep = new ModCompanyRep(columns[0], columns[1], "password");
                    rep.setDepartment(columns[3]);
                    rep.setPosition(columns[4]);
                    rep.setEmail(columns[5]);
                    rep.setAuthorised(Boolean.parseBoolean(columns[6]));
                    ModCompany company = new ModCompany(columns[2]);
                    rep.setCompany(company);
                    return rep;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to lookup modular company rep: " + e.getMessage());
        }
        return null;
    }

    public boolean existsById(String id) {
        return findById(id) != null;
    }

    public void saveRecord(ModCompanyRep rep) throws IOException {
        if (rep == null) return;
        File file = csvPath.toFile();
        boolean writeHeader = !file.exists() || file.length() == 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                writer.write("CompanyRepID,Name,CompanyName,Department,Position,Email,Approved");
                writer.newLine();
            }
            String companyName = rep.getCompany() != null ? rep.getCompany().getCompanyName() : "";
            String record = String.join(",",
                    rep.getId(),
                    rep.getName(),
                    companyName,
                    rep.getDepartment(),
                    rep.getPosition(),
                    rep.getEmail(),
                    String.valueOf(rep.isAuthorised()));
            writer.write(record);
            writer.newLine();
        }
    }
}
