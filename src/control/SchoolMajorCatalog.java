// documented

package control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and provides access to a catalog of schools and their respective majors.
 *
 * <p>The catalog is sourced from a CSV file in the format:</p>
 *
 * <pre>
 * School,Major
 * School of Computing,Computer Science
 * School of Computing,Information Systems
 * College of Engineering,Mechanical Engineering
 * ...
 * </pre>
 *
 * <p>The first header row (starting with "School") is automatically skipped.</p>
 *
 * <p>Majors are stored case-insensitively per school, and duplicates within the
 * same school are ignored.</p>
 */

public class SchoolMajorCatalog {

    /** Internal map linking each school to its list of majors. */
    private final Map<String, List<String>> majorsBySchool = new LinkedHashMap<>();

    /**
     * Creates a catalog and immediately attempts to load data from the provided CSV file.
     *
     * @param csvFile the CSV containing school-major pairs
     */
    public SchoolMajorCatalog(File csvFile) {
        load(csvFile);
    }

    /**
     * Loads school–major mappings from the given CSV file.
     *
     * <p>The method is resilient to:</p>
     * <ul>
     *     <li>Missing or unreadable files</li>
     *     <li>Blank lines</li>
     *     <li>Rows without both school and major</li>
     *     <li>Header row beginning with "School"</li>
     *     <li>Duplicate majors (case-insensitive)</li>
     * </ul>
     *
     * @param file the CSV to load
     */
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

    /**
     * Returns whether the catalog contains no school-major data.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return majorsBySchool.isEmpty();
    }

    /**
     * Returns the list of schools recorded in the catalog.
     *
     * @return list of school names
     */
    public List<String> getSchools() {
        return new ArrayList<>(majorsBySchool.keySet());
    }

    /**
     * Returns the list of majors offered by a given school.
     *
     * @param school the school whose majors are requested
     * @return list of majors or an empty list if the school does not exist
     */
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
