package modular.repository;

import modular.model.ModStudent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModStudentRepository {
    private final Path csvPath;

    public ModStudentRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    public List<ModStudent> loadAll() {
        List<ModStudent> students = new ArrayList<>();
        if (csvPath == null || !Files.exists(csvPath)) {
            return students;
        }
        try (var reader = Files.newBufferedReader(csvPath)) {
            reader.readLine(); // header
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] columns = line.split(",", -1);
                if (columns.length < 5) continue;
                ModStudent student = new ModStudent(columns[0], columns[1], "password", columns[3], columns[2]);
                student.setEmail(columns[4]);
                students.add(student);
            }
        } catch (IOException e) {
            System.out.println("Failed to load modular students: " + e.getMessage());
        }
        return students;
    }

    public boolean existsById(String studentId) {
        if (studentId == null || csvPath == null || !Files.exists(csvPath)) {
            return false;
        }
        try (var reader = Files.newBufferedReader(csvPath)) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] columns = line.split(",", -1);
                if (columns.length > 0 && columns[0].equalsIgnoreCase(studentId)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to verify modular student records: " + e.getMessage());
        }
        return false;
    }

    public void saveRecord(ModStudent student) throws IOException {
        if (student == null) return;
        File file = csvPath.toFile();
        boolean writeHeader = !file.exists() || file.length() == 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                writer.write("StudentID,Name,Major,Year,Email");
                writer.newLine();
            }
            String record = String.join(",",
                    student.getId(),
                    student.getName(),
                    student.getMajor(),
                    student.getYear(),
                    student.getEmail());
            writer.write(record);
            writer.newLine();
        }
    }
}
