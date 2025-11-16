package control;

import entity.Internship;
import entity.InternshipStatus;
import entity.Student;
import entity.User;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InternshipManager {
    private final List<FilterSettings> userFilters = new ArrayList<>();
    private ArrayList<Internship> internships = new ArrayList<>();

    public ArrayList<Internship> getInternships() {
        return internships;
    }

    public void setInternships(ArrayList<Internship> internships) {
        this.internships = internships != null ? internships : new ArrayList<>();
    }

    public FilterSettings getOrCreateFilterSettings(String userId) {
        String key = normalizeUserId(userId);
        for (FilterSettings settings : userFilters) {
            if (settings.getUserId().equals(key)) {
                return settings;
            }
        }
        FilterSettings settings = new FilterSettings(key);
        userFilters.add(settings);
        return settings;
    }

    public void saveUserFilters(String userId, String status, String major, String level, String company, Date closingStart, Date closingEnd) {
        getOrCreateFilterSettings(userId).update(status, major, level, company, closingStart, closingEnd);
    }

    public List<Internship> getFilteredInternships(String userId) {
        FilterSettings filters = getOrCreateFilterSettings(userId);
        List<Internship> base = filterBy(i ->
                matchesStatus(i, filters.getStatus()) &&
                matchesMajor(i, filters.getMajor()) &&
                matchesLevel(i, filters.getLevel()) &&
                matchesCompany(i, filters.getCompany()) &&
                matchesClosingDate(i, filters.getClosingStart(), filters.getClosingEnd()));
        return base;
    }

    public void run(User user, Scanner scanner) {
        if (user == null) {
            System.out.println("No user selected.");
            return;
        }
        if (scanner == null) {
            System.out.println("No input available.");
            return;
        }
        if (internships.isEmpty()) {
            System.out.println("No internships available.");
            return;
        }
        FilterSettings settings = getOrCreateFilterSettings(user.getId());
        if (user instanceof Student student) {
            settings.update("APPROVED", student.getMajor(), null, null, null, null);
            List<Internship> opportunities = applyClosingDateFilter(
                    getVisibleInternships(student),
                    settings.getClosingStart(),
                    settings.getClosingEnd()
            );
            if (opportunities.isEmpty()) {
                System.out.println("No internships available for your profile at the moment.");
                return;
            }
            System.out.println("Available internships:");
            for (int i = 0; i < opportunities.size(); i++) {
                Internship internship = opportunities.get(i);
                String companyName = internship.getCompany() != null
                        ? safeValue(internship.getCompany().getCompanyName())
                        : "Unknown Company";
                System.out.printf("%d. %s at %s [%s]%n",
                        i + 1,
                        safeValue(internship.getTitle()),
                        companyName,
                        safeValue(internship.getLevel()));
                System.out.println("   Preferred major: " + safeValue(internship.getPreferredMajor()));
                if (internship.getOpenDate() != null || internship.getCloseDate() != null) {
                    System.out.println("   Open: " + internship.getOpenDate() + " | Close: " + internship.getCloseDate());
                }
            }
            System.out.print("Enter internship number to apply (or press Enter to skip): ");
            String selection = scanner.nextLine().trim();
            if (!selection.isEmpty()) {
                try {
                    int idx = Integer.parseInt(selection);
                    if (idx < 1 || idx > opportunities.size()) {
                        System.out.println("Invalid selection.");
                    } else if (student.applyInternship(opportunities.get(idx - 1))) {
                        System.out.println("Application submitted.");
                    } else {
                        System.out.println("Unable to submit application.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid selection.");
                }
            }
            System.out.printf("Current closing date filter: %s to %s%n",
                    formatDate(settings.getClosingStart()),
                    formatDate(settings.getClosingEnd()));
            Date startFilter = promptDate(scanner, "Enter closing-date start (yyyy-MM-dd) or leave blank");
            Date endFilter = promptDate(scanner, "Enter closing-date end (yyyy-MM-dd) or leave blank");
            if (startFilter != null || endFilter != null) {
                settings.update(null, null, null, null, startFilter, endFilter);
                List<Internship> filtered = applyClosingDateFilter(
                        getVisibleInternships(student),
                        settings.getClosingStart(),
                        settings.getClosingEnd()
                );
                if (filtered.isEmpty()) {
                    System.out.println("No internships fall within the specified closing-date range.");
                } else {
                    System.out.println("Internships within your closing-date range:");
                    for (int i = 0; i < filtered.size(); i++) {
                        Internship internship = filtered.get(i);
                        String companyName = internship.getCompany() != null
                                ? safeValue(internship.getCompany().getCompanyName())
                                : "Unknown Company";
                        System.out.printf("%d. %s at %s [%s] | Close: %s%n",
                                i + 1,
                                safeValue(internship.getTitle()),
                                companyName,
                                safeValue(internship.getLevel()),
                                formatDate(internship.getCloseDate()));
                    }
                }
            }
            return;
        }
        printCurrentFilters(settings);
        System.out.print("Update filters? (y/n): ");
        String choice = scanner.nextLine().trim();
        if (choice.equalsIgnoreCase("y")) {
            configureFilters(user, scanner);
        }
        List<Internship> filtered = getFilteredInternships(user.getId());
        if (filtered.isEmpty()) {
            System.out.println("No internships match the current filters.");
            return;
        }
        System.out.println("Internships matching your filters:");
        for (Internship internship : filtered) {
            String companyName = internship.getCompany() != null
                    ? safeValue(internship.getCompany().getCompanyName())
                    : "Unknown Company";
            System.out.printf("- %s at %s [%s] Status=%s Major=%s Closing=%s%n",
                    safeValue(internship.getTitle()),
                    companyName,
                    safeValue(internship.getLevel()),
                    internship.getStatus(),
                    safeValue(internship.getPreferredMajor()),
                    internship.getCloseDate());
        }
    }

    public List<Internship> filterBy(Predicate<Internship> criteria) {
        List<Internship> result = new ArrayList<>();
        for (Internship i : internships) {
            if (criteria.test(i)) {
                result.add(i);
            }
        }
        return result;
    }

    public List<Internship> getVisibleInternships(Student student) {
        return filterBy(i ->
            i.isOpen() &&
                    i.getPreferredMajor() != null &&
                    i.getPreferredMajor().equalsIgnoreCase(student.getMajor()) &&
                    isEligibleByYearLevel(student, i)
        );
    }

    private boolean matchesStatus(Internship internship, String filter) {
        if (isAll(filter)) return true;
        InternshipStatus status = internship.getStatus();
        return status != null && status.name().equalsIgnoreCase(filter);
    }

    private boolean matchesMajor(Internship internship, String filter) {
        if (isAll(filter)) return true;
        return internship.getPreferredMajor() != null &&
                internship.getPreferredMajor().equalsIgnoreCase(filter);
    }

    private boolean matchesLevel(Internship internship, String filter) {
        if (isAll(filter)) return true;
        return internship.getLevel() != null &&
                internship.getLevel().equalsIgnoreCase(filter);
    }

    private boolean matchesCompany(Internship internship, String filter) {
        if (isAll(filter)) return true;
        return internship.getCompany() != null &&
                internship.getCompany().getCompanyName() != null &&
                internship.getCompany().getCompanyName().equalsIgnoreCase(filter);
    }

    private boolean matchesClosingDate(Internship internship, Date start, Date end) {
        if (start == null && end == null) return true;
        Date close = internship.getCloseDate();
        if (close == null) return false;
        if (start != null && close.before(start)) return false;
        if (end != null && close.after(end)) return false;
        return true;
    }

    private List<Internship> applyClosingDateFilter(List<Internship> source, Date start, Date end) {
        if (start == null && end == null) {
            return new ArrayList<>(source);
        }
        List<Internship> filtered = new ArrayList<>();
        for (Internship internship : source) {
            if (matchesClosingDate(internship, start, end)) {
                filtered.add(internship);
            }
        }
        return filtered;
    }

    private boolean isEligibleByYearLevel(Student s, Internship i){
        String level = i.getLevel();
        if (level == null) return false;

        if (s.getYear().equals("2") || s.getYear().equals("1")){
            return level.equalsIgnoreCase("Basic");
        }

        return level.equalsIgnoreCase("Basic")
                || level.equalsIgnoreCase("Intermediate")
                || level.equalsIgnoreCase("Advanced");
    }

    public List<Internship> getAllInternships() {
        return new ArrayList<>(internships);
    }

    private boolean isAll(String value) {
        return value == null || value.isBlank() || value.equalsIgnoreCase("All");
    }

    private String normalizeUserId(String input) {
        return (input == null || input.isBlank()) ? "anonymous" : input;
    }

    private void printCurrentFilters(FilterSettings settings) {
        System.out.printf("Current filters -> Status: %s, Major: %s, Level: %s, Company: %s, Closing Start: %s, Closing End: %s%n",
                settings.getStatus(),
                settings.getMajor(),
                settings.getLevel(),
                settings.getCompany(),
                formatDate(settings.getClosingStart()),
                formatDate(settings.getClosingEnd()));
    }

    private void configureFilters(User user, Scanner scanner) {
        FilterSettings current = getOrCreateFilterSettings(user.getId());
        System.out.println("Enter new filter values (press Enter to keep current).");
        System.out.println("Status options: All, Pending, Approved, Rejected, Filled");
        String status = promptValue(scanner, "Status", current.getStatus());
        List<String> majors = getAvailableMajors();
        if (!majors.isEmpty()) {
            System.out.println("Available majors: " + String.join(", ", majors));
        }
        String major = promptValue(scanner, "Preferred major", current.getMajor());
        System.out.println("Levels: Basic, Intermediate, Advanced");
        String level = promptValue(scanner, "Level", current.getLevel());
        String company = promptValue(scanner, "Company name", current.getCompany());
        Date closingStart = promptDate(scanner, "Closing date start (yyyy-MM-dd) or leave blank");
        Date closingEnd = promptDate(scanner, "Closing date end (yyyy-MM-dd) or leave blank");
        saveUserFilters(user.getId(), status, major, level, company, closingStart, closingEnd);
        System.out.println("Filters updated.");
    }

    private String promptValue(Scanner scanner, String label, String currentValue) {
        System.out.printf("%s [%s]: ", label, currentValue);
        return scanner.nextLine().trim();
    }

    private List<String> getAvailableMajors() {
        return internships.stream()
                .map(Internship::getPreferredMajor)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
    }

    private String safeValue(String value) {
        return (value == null || value.isBlank()) ? "N/A" : value;
    }

    private Date promptDate(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + ": ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return parseDate(input);
            } catch (ParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
    }

    private Date parseDate(String input) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setLenient(false);
        return format.parse(input);
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "Any";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }
}
