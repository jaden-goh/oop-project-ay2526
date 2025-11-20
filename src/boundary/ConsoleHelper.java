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

public class ConsoleHelper {
    private final Scanner scanner;
    private final SchoolMajorCatalog schoolMajorCatalog;

    public ConsoleHelper(Scanner scanner, SchoolMajorCatalog schoolMajorCatalog) {
        this.scanner = scanner;
        this.schoolMajorCatalog = schoolMajorCatalog;
    }

    public String readLine(String prompt) {
        if (prompt != null && !prompt.isBlank()) {
            System.out.print(prompt);
        }
        return scanner.nextLine().trim();
    }

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

    public int readInt(String prompt, int min, int max) {
        Integer value = readInt(prompt, min, max, null, false);
        return value == null ? min : value;
    }

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

    public InternshipLevel promptInternshipLevel() {
        InternshipLevel[] levels = InternshipLevel.values();
        for (int i = 0; i < levels.length; i++) {
            System.out.println((i + 1) + ". " + levels[i]);
        }
        int choice = readInt("Select internship level: ", 1, levels.length);
        return levels[choice - 1];
    }

    public InternshipStatus promptStatusSelection() {
        InternshipStatus[] statuses = InternshipStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i]);
        }
        int choice = readInt("Select a status: ", 1, statuses.length);
        return statuses[choice - 1];
    }

    public String promptStudentMajorSelection() {
        String major = promptMajorSelectionFromCatalog(true, "Major: ");
        while (major == null || major.isBlank()) {
            System.out.println("Major cannot be empty.");
            major = promptManualMajorInput("Major: ");
        }
        return major;
    }

    public String promptPreferredMajorSelection() {
        while (true) {
            String major = promptMajorSelectionFromCatalog(true, "Preferred major: ");
            if (major != null && !major.isBlank()) {
                return major;
            }
            System.out.println("Preferred major is required.");
        }
    }

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

    public String formatPreferredMajors(Internship internship) {
        if (internship == null) {
            return "Any";
        }
        String preferred = internship.getPreferredMajor();
        return preferred == null || preferred.isBlank() ? "Any" : preferred;
    }
}
