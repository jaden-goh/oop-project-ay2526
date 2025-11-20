// documented

package boundary;

import control.SchoolMajorCatalog;
import entity.Application;
import entity.Internship;
import entity.InternshipLevel;
import entity.InternshipStatus;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Utility class for all console input/output interactions.
 *
 * <p>This class supports:</p>
 * <ul>
 *     <li>Reading strings, integers, and dates</li>
 *     <li>Prompting yes/no decisions</li>
 *     <li>Selecting internship levels and statuses</li>
 *     <li>Selecting majors (from catalog or manual entry)</li>
 *     <li>Printing internship rows</li>
 *     <li>Selecting internships and applications from lists</li>
 * </ul>
 *
 * <p>Acts as a reusable boundary/helper component across menus.</p>
 */
public class ConsoleHelper {

    /** Scanner for reading console input. */
    private final Scanner scanner;

    /** School-major catalog used when selecting majors. */
    private final SchoolMajorCatalog schoolMajorCatalog;

    /**
     * Constructs a console helper.
     *
     * @param scanner            scanner for user input
     * @param schoolMajorCatalog optional catalog for selecting majors
     */
    public ConsoleHelper(Scanner scanner, SchoolMajorCatalog schoolMajorCatalog) {
        this.scanner = scanner;
        this.schoolMajorCatalog = schoolMajorCatalog;
    }

    /**
     * Reads a line of input from the user.
     *
     * @param prompt message displayed before input
     * @return trimmed user input
     */
    public String readLine(String prompt) {
        if (prompt != null && !prompt.isBlank()) {
            System.out.print(prompt);
        }
        return scanner.nextLine().trim();
    }

    /**
     * Reads a password with basic validation (length ≥ 8).
     * User may type "cancel" to abort.
     *
     * @param prompt prompt text
     * @return password string or null if cancelled/invalid
     */
    public String promptPasswordInput(String prompt) {
        String password = readLine(prompt);
        if ("cancel".equalsIgnoreCase(password)) {
            System.out.println("Action cancelled.");
            return null;
        }
        if (password.length() < 8) {
            System.out.println("Password must be at least 8 characters long.");
            return null;
        }
        return password;
    }

    /**
     * Repeatedly prompts user for a yes/no response.
     *
     * @param prompt     prompt text
     * @param defaultYes unused default value
     * @return true for yes, false for no
     */
    public boolean promptYesNo(String prompt, boolean defaultYes) {
        while (true) {
            String input = readLine(prompt);
            if ("y".equalsIgnoreCase(input) || "yes".equalsIgnoreCase(input)) {
                return true;
            }
            if ("n".equalsIgnoreCase(input) || "no".equalsIgnoreCase(input)) {
                return false;
            }
            System.out.println("Please enter y or n.");
        }
    }

    /**
     * Reads a constrained integer from the user.
     *
     * @param prompt       message to display
     * @param min          minimum allowed value
     * @param max          maximum allowed value
     * @param defaultValue optional default if input is empty
     * @param allowCancel  whether "cancel" returns null
     * @return integer value or null on cancel
     */
    public Integer readInt(String prompt, int min, int max, Integer defaultValue, boolean allowCancel) {
        while (true) {
            String input = readLine(prompt);
            if (input.isEmpty() && defaultValue != null) {
                return defaultValue;
            }
            if (allowCancel && "cancel".equalsIgnoreCase(input)) {
                return null;
            }
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    throw new NumberFormatException();
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            }
        }
    }

    /**
     * Reads an integer within a range.
     */
    public int readInt(String prompt, int min, int max) {
        Integer value = readInt(prompt, min, max, null, false);
        return value == null ? min : value;
    }

    /**
     * Reads an optional date from user input.
     *
     * @param prompt prompt text
     * @return parsed LocalDate or null if blank input
     */
    public LocalDate readOptionalDate(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use yyyy-MM-dd.");
            }
        }
    }

    /**
     * Prompts user to select an internship level.
     *
     * @return chosen InternshipLevel
     */
    public InternshipLevel promptInternshipLevel() {
        InternshipLevel[] levels = InternshipLevel.values();
        for (int i = 0; i < levels.length; i++) {
            System.out.println((i + 1) + ". " + levels[i]);
        }
        int choice = readInt("Select internship level: ", 1, levels.length);
        return levels[choice - 1];
    }

    /**
     * Prompts user to select an internship status.
     *
     * @return chosen InternshipStatus
     */
    public InternshipStatus promptStatusSelection() {
        InternshipStatus[] statuses = InternshipStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i]);
        }
        int choice = readInt("Select a status: ", 1, statuses.length);
        return statuses[choice - 1];
    }

    /**
     * Prompts student major selection (from catalog when available).
     *
     * @return selected major (never empty)
     */
    public String promptStudentMajorSelection() {
        String major = promptMajorSelectionFromCatalog(true, "Major: ");
        while (major == null || major.isBlank()) {
            System.out.println("Major cannot be empty.");
            major = promptManualMajorInput("Major: ");
        }
        return major;
    }

    /**
     * Prompts selection of preferred major.
     *
     * @return selected major
     */
    public String promptPreferredMajorSelection() {
        while (true) {
            String major = promptMajorSelectionFromCatalog(true, "Preferred major: ");
            if (major != null && !major.isBlank()) {
                return major;
            }
            System.out.println("Preferred major is required.");
        }
    }

    /**
     * Prompts user to select an internship from a list.
     *
     * @param internships list to display
     * @return chosen internship or null if cancelled
     */
    public Internship selectInternshipFromList(List<Internship> internships) {
        if (internships == null || internships.isEmpty()) {
            System.out.println("No internships to select.");
            return null;
        }
        for (int i = 0; i < internships.size(); i++) {
            printInternshipRow(i + 1, internships.get(i));
        }
        int choice = readInt("Select an internship (0 to cancel): ", 0, internships.size());
        if (choice == 0) {
            return null;
        }
        return internships.get(choice - 1);
    }

    /**
     * Prompts user to select an application from a list.
     *
     * @param applications list of applications
     * @param prompt       prompt message
     * @return selected Application or null if cancelled
     */
    public Application selectApplicationFromList(List<Application> applications, String prompt) {
        if (applications == null || applications.isEmpty()) {
            System.out.println("No applications available.");
            return null;
        }
        for (int i = 0; i < applications.size(); i++) {
            Application app = applications.get(i);
            System.out.println((i + 1) + ". " + app.getInternship().getTitle()
                    + " - " + app.getStatus());
        }
        int choice = readInt(prompt, 0, applications.size());
        if (choice == 0) {
            return null;
        }
        return applications.get(choice - 1);
    }

    /**
     * Prints a single internship summary row.
     */
    public void printInternshipRow(int index, Internship internship) {
        int totalSlots = internship.getSlots().size();
        long filledSlots = internship.getSlots().stream()
                .filter(slot -> slot.getAssignedStudent() != null)
                .count();
        System.out.println(index + ". " + internship.getTitle() + " (" + internship.getCompanyName() + ")"
                + " | Status: " + internship.getStatus()
                + " | Level: " + internship.getLevel()
                + " | Major: " + formatPreferredMajors(internship)
                + " | Visibility: " + (internship.isVisible() ? "On" : "Off")
                + " | Slots: " + filledSlots + "/" + totalSlots);
    }

    /**
     * Internal helper for selecting a major (from catalog or manual entry).
     */
    private String promptMajorSelectionFromCatalog(boolean allowManualEntry, String manualPrompt) {
        if (schoolMajorCatalog == null || schoolMajorCatalog.isEmpty()) {
            return allowManualEntry ? promptManualMajorInput(manualPrompt) : null;
        }
        List<String> schools = schoolMajorCatalog.getSchools();
        if (schools.isEmpty()) {
            return allowManualEntry ? promptManualMajorInput(manualPrompt) : null;
        }
        while (true) {
            System.out.println("\nSelect a school:");
            for (int i = 0; i < schools.size(); i++) {
                System.out.println((i + 1) + ". " + schools.get(i));
            }
            if (allowManualEntry) {
                System.out.println("0. Enter major manually");
            } else {
                System.out.println("0. Cancel selection");
            }
            int choice = readInt("Choice: ", 0, schools.size());
            if (choice == 0) {
                return allowManualEntry ? promptManualMajorInput(manualPrompt) : null;
            }
            String school = schools.get(choice - 1);
            String major = promptMajorSelectionForSchool(school);
            if (major != null) {
                return major;
            }
        }
    }

    /**
     * Internal helper to select a major within a chosen school.
     */
    private String promptMajorSelectionForSchool(String school) {
        List<String> majors = schoolMajorCatalog.getMajorsForSchool(school);
        if (majors.isEmpty()) {
            System.out.println("No majors found for " + school + ". Please choose another school.");
            return null;
        }
        while (true) {
            System.out.println("\nSelect a major from " + school + ":");
            for (int i = 0; i < majors.size(); i++) {
                System.out.println((i + 1) + ". " + majors.get(i));
            }
            System.out.println("0. Back to school list");
            int choice = readInt("Choice: ", 0, majors.size());
            if (choice == 0) {
                return null;
            }
            return majors.get(choice - 1);
        }
    }

    /**
     * Manual input fallback for major selection.
     *
     * @param prompt prompt text
     * @return manually entered major
     */
    private String promptManualMajorInput(String prompt) {
        while (true) {
            if (prompt != null && !prompt.isBlank()) {
                System.out.print(prompt);
            }
            String input = scanner.nextLine().trim();
            if (!input.isBlank()) {
                return input;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    /**
     * Formats preferred major for display.
     *
     * @param internship internship object
     * @return formatted preferred major ("Any" if none)
     */
    public String formatPreferredMajors(Internship internship) {
        if (internship == null) {
            return "Any";
        }
        String preferred = internship.getPreferredMajor();
        return preferred == null || preferred.isBlank() ? "Any" : preferred;
    }
}
