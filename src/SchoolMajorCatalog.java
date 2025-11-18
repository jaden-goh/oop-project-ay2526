import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SchoolMajorCatalog {
    private final Map<String, List<String>> majorsBySchool = new LinkedHashMap<>();

    public SchoolMajorCatalog(File csvFile) {
        load(csvFile);
    }

    private void load(File file) {
        if (file == null || !file.exists()) {
            System.err.println("School-major CSV not found: " + (file == null ? "null" : file.getPath()));
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (!headerSkipped && trimmed.toLowerCase().startsWith("school")) {
                    headerSkipped = true;
                    continue;
                }
                String[] tokens = trimmed.split(",", 2);
                if (tokens.length < 2) {
                    continue;
                }
                String school = tokens[0].trim();
                String major = tokens[1].trim();
                if (school.isEmpty() || major.isEmpty()) {
                    continue;
                }
                majorsBySchool.computeIfAbsent(school, key -> new ArrayList<>());
                List<String> majors = majorsBySchool.get(school);
                if (majors.stream().noneMatch(existing -> existing.equalsIgnoreCase(major))) {
                    majors.add(major);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load school-major catalog: " + e.getMessage());
        }
    }

    public boolean isEmpty() {
        return majorsBySchool.isEmpty();
    }

    public List<String> getSchools() {
        return new ArrayList<>(majorsBySchool.keySet());
    }

    public List<String> getMajorsForSchool(String school) {
        if (school == null) {
            return Collections.emptyList();
        }
        List<String> majors = majorsBySchool.get(school);
        if (majors == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(majors);
    }
}
