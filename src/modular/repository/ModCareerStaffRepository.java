package modular.repository;

import modular.model.ModCareerCenterStaff;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModCareerStaffRepository {
    private final Path csvPath;

    public ModCareerStaffRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    public List<ModCareerCenterStaff> loadAll() {
        List<ModCareerCenterStaff> staff = new ArrayList<>();
        if (csvPath == null || !Files.exists(csvPath)) {
            return staff;
        }
        try (var reader = Files.newBufferedReader(csvPath)) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] columns = line.split(",", -1);
                if (columns.length < 5) continue;
                ModCareerCenterStaff member = new ModCareerCenterStaff(columns[0], columns[1], "password", columns[2], columns[3]);
                member.setEmail(columns[4]);
                staff.add(member);
            }
        } catch (IOException e) {
            System.out.println("Failed to load modular staff: " + e.getMessage());
        }
        return staff;
    }

    public boolean existsById(String id) {
        if (id == null || csvPath == null || !Files.exists(csvPath)) return false;
        try (var reader = Files.newBufferedReader(csvPath)) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] columns = line.split(",", -1);
                if (columns.length > 0 && columns[0].equalsIgnoreCase(id)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to verify staff record: " + e.getMessage());
        }
        return false;
    }

    public void saveRecord(ModCareerCenterStaff staff) throws IOException {
        if (staff == null) return;
        File file = csvPath.toFile();
        boolean writeHeader = !file.exists() || file.length() == 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                writer.write("StaffID,Name,Role,Department,Email");
                writer.newLine();
            }
            String record = String.join(",",
                    staff.getId(),
                    staff.getName(),
                    staff.getRole(),
                    staff.getDepartment(),
                    staff.getEmail());
            writer.write(record);
            writer.newLine();
        }
    }
}
