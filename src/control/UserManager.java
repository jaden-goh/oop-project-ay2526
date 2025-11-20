// documented

package control;

import entity.AccountRequest;
import entity.CareerCenterStaff;
import entity.CompanyRep;
import entity.Student;
import entity.User;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Manages all user-related operations in the system.
 *
 * <p>Responsibilities include:</p>
 * <ul>
 *     <li>Loading users (students, staff, company reps) from CSV files</li>
 *     <li>Registration of new users</li>
 *     <li>Login and password reset</li>
 *     <li>Tracking and processing company representative account requests</li>
 *     <li>Providing filtered views of account requests and staff members</li>
 * </ul>
 */
public class UserManager {

    /** Validation pattern for student IDs (e.g., U1234567A). */
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^U\\d{7}[A-Z]$");

    /** Validation pattern for career center staff IDs (e.g., ABC123). */
    private static final Pattern STAFF_ID_PATTERN = Pattern.compile("^[A-Za-z]{3}\\d{3}$");

    /** Simple email format pattern for company representative IDs. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Default page size for paginated pending account request listing. */
    private static final int DEFAULT_PENDING_PAGE_SIZE = 10;

    /** All registered users in the system. */
    private final List<User> users = new ArrayList<>();

    /** All company representative account approval requests. */
    private final List<AccountRequest> accountRequests = new ArrayList<>();

    /** Last login-related message (e.g., success/failure reason) for display. */
    private String lastLoginMessage = "";

    /**
     * Loads all users (students, staff, company representatives) from their respective files.
     *
     * @param studentFile CSV file containing student records
     * @param staffFile   CSV file containing staff records
     * @param companyFile CSV file containing company representative records
     */
    public void loadAllUsers(File studentFile, File staffFile, File companyFile) {
        loadStudents(studentFile);
        loadStaff(staffFile);
        loadCompanyRepresentatives(companyFile);
    }

    /**
     * Logs in a user with the given ID and password.
     *
     * <p>Special handling for company representatives:</p>
     * <ul>
     *     <li>If the rep is not yet approved, login is blocked and a message is set.</li>
     *     <li>If the latest account request is rejected, the rejection notes are shown.</li>
     * </ul>
     *
     * @param id   user ID
     * @param pass password
     * @return the logged-in {@link User}, or null if login fails
     */
    public User login(String id, String pass) {
        if (id == null || id.isBlank()) {
            lastLoginMessage = "User ID is required. Enter your assigned ID or register first.";
            return null;
        }
        if (pass == null || pass.isBlank()) {
            lastLoginMessage = "Password is required. Use the reset option from the login screen if forgotten.";
            return null;
        }
        String trimmedId = id.trim();
        User target = findUserById(trimmedId);
        if (target == null) {
            lastLoginMessage = "No account was found for ID '" + trimmedId + "'. Use the registration option if needed.";
            return null;
        }
        if (!target.login(pass)) {
            lastLoginMessage = "Incorrect password. Use the \"reset\" keyword at the prompt to create a new one.";
            return null;
        }
        if (target instanceof CompanyRep rep && !rep.isApproved()) {
            AccountRequest latestRequest = findLatestRequestForRep(rep.getUserID());
            if (latestRequest != null && AccountRequest.STATUS_REJECTED.equals(latestRequest.getStatus())) {
                String approverName = latestRequest.getApprover() != null
                        ? latestRequest.getApprover().getName()
                        : "Career Center staff";
                StringBuilder builder = new StringBuilder("Account rejected by ").append(approverName);
                if (latestRequest.getDecisionNotes() != null && !latestRequest.getDecisionNotes().isBlank()) {
                    builder.append(". Notes: ").append(latestRequest.getDecisionNotes());
                } else {
                    builder.append(". Contact the Career Center for details.");
                }
                lastLoginMessage = builder.toString();
            } else {
                lastLoginMessage = "Company representative account pending approval. Career Center staff will notify you once approved.";
            }
            return null;
        }
        lastLoginMessage = "Login successful.";
        return target;
    }

    /**
     * Returns the last login message with an appended help hint.
     *
     * @return a user-friendly login message
     */
    public String getLastLoginMessage() {
        return lastLoginMessage + " \nNeed help? Contact the Career Center.";
    }

    /**
     * Attempts to reset the password of a user identified by ID.
     *
     * @param id          user ID
     * @param newPassword the new password
     * @return true if the password was successfully updated, false otherwise
     */
    public boolean resetPassword(String id, String newPassword) {
        User user = findUserById(id);
        if (user == null) {
            return false;
        }
        try {
            user.changePassword(newPassword);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Registers a new student, performing format and validity checks.
     *
     * @param id       student ID (must match NTU pattern)
     * @param name     student name
     * @param password password (minimum 8 characters)
     * @param year     year of study (1–4)
     * @param major    student major
     * @return true if registration succeeds, false if validation fails or user already exists
     */
    public boolean registerStudent(String id, String name, String password, int year, String major) {
        if (id == null || !STUDENT_ID_PATTERN.matcher(id.trim()).matches()) {
            return false;
        }
        if (name == null || name.isBlank()) {
            return false;
        }
        if (password == null || password.trim().length() < 8) {
            return false;
        }
        if (year < 1 || year > 4) {
            return false;
        }
        Student student = new Student(id.trim(), name.trim(), password, year, major == null ? "" : major.trim());
        return addUser(student);
    }

    /**
     * Registers a new company representative and creates an associated account request.
     *
     * @param id          representative ID (must be a valid email)
     * @param name        representative name
     * @param password    password
     * @param companyName company name
     * @param department  department (optional)
     * @param position    position (optional)
     * @param approved    whether the account is pre-approved
     * @return true if registration succeeds, false otherwise
     */
    public boolean registerCompanyRep(String id, String name, String password,
                                      String companyName, String department, String position,
                                      boolean approved) {
        if (id == null || !EMAIL_PATTERN.matcher(id.trim()).matches()) {
            return false;
        }
        if (name == null || name.isBlank()) {
            return false;
        }
        if (password == null || password.trim().length() < 8) {
            return false;
        }
        if (companyName == null || companyName.isBlank()) {
            return false;
        }
        CompanyRep representative = new CompanyRep(id.trim(), name.trim(), password, companyName.trim(),
                department == null ? "" : department.trim(), position == null ? "" : position.trim(), approved);
        if (!addUser(representative)) {
            return false;
        }
        AccountRequest request = new AccountRequest(representative);
        if (approved) {
            representative.setApproved(true);
            request.setStatus(AccountRequest.STATUS_APPROVED);
        }
        accountRequests.add(request);
        return true;
    }

    /**
     * Registers a new career center staff member.
     *
     * @param id         staff ID (must match staff ID pattern)
     * @param name       staff name
     * @param password   password
     * @param department staff department
     * @return true if registration succeeds, false otherwise
     */
    public boolean registerCareerCenterStaff(String id, String name, String password, String department) {
        if (id == null || !STAFF_ID_PATTERN.matcher(id.trim()).matches()) {
            return false;
        }
        if (name == null || name.isBlank()) {
            return false;
        }
        if (password == null || password.trim().length() < 8) {
            return false;
        }
        CareerCenterStaff staff = new CareerCenterStaff(id.trim(), name.trim(), password,
                department == null ? "" : department.trim());
        return addUser(staff);
    }

    /**
     * Approves a company representative account request.
     *
     * @param repId    representative ID
     * @param approver staff member who approves the request
     * @return true if the request exists and is approved; false otherwise
     */
    public boolean approveRepresentative(String repId, CareerCenterStaff approver) {
        if (repId == null || approver == null) {
            return false;
        }
        AccountRequest request = findRequest(repId.trim());
        if (request == null) {
            return false;
        }
        request.setApprover(approver);
        request.setStatus(AccountRequest.STATUS_APPROVED);
        CompanyRep rep = request.getRep();
        if (rep != null) {
            rep.setApproved(true);
        }
        return true;
    }

    /**
     * Rejects a company representative account request, setting optional notes.
     *
     * @param repId    representative ID
     * @param approver staff member who rejects the request
     * @param notes    optional rejection notes
     * @return true if the request exists and is rejected; false otherwise
     */
    public boolean rejectRepresentative(String repId, CareerCenterStaff approver, String notes) {
        if (repId == null || approver == null) {
            return false;
        }
        AccountRequest request = findRequest(repId.trim());
        if (request == null) {
            return false;
        }
        request.setApprover(approver);
        request.setDecisionNotes(notes);
        request.setStatus(AccountRequest.STATUS_REJECTED);
        CompanyRep rep = request.getRep();
        if (rep != null) {
            rep.setApproved(false);
        }
        return true;
    }

    /**
     * Returns the first page of pending account requests using the default page size.
     *
     * @return list of pending account requests
     */
    public List<AccountRequest> getPendingAccounts() {
        return getPendingAccounts(1, DEFAULT_PENDING_PAGE_SIZE, AccountRequest.STATUS_PENDING);
    }

    /**
     * Returns a paginated list of account requests filtered by status.
     *
     * @param page         page number (1-based)
     * @param pageSize     number of items per page
     * @param statusFilter status string or "ALL" (case-insensitive)
     * @return unmodifiable list of matching account requests on the given page
     */
    public List<AccountRequest> getPendingAccounts(int page, int pageSize, String statusFilter) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = DEFAULT_PENDING_PAGE_SIZE;
        }
        String filter = statusFilter == null ? AccountRequest.STATUS_PENDING : statusFilter.trim();
        List<AccountRequest> matches = new ArrayList<>();
        for (AccountRequest request : accountRequests) {
            if ("ALL".equalsIgnoreCase(filter) || request.getStatus().equalsIgnoreCase(filter)) {
                matches.add(request);
            }
        }
        int fromIndex = Math.min(matches.size(), (page - 1) * pageSize);
        int toIndex = Math.min(matches.size(), fromIndex + pageSize);
        return Collections.unmodifiableList(matches.subList(fromIndex, toIndex));
    }

    /**
     * Returns all registered career center staff members.
     *
     * @return unmodifiable list of staff members
     */
    public List<CareerCenterStaff> getCareerCenterStaffMembers() {
        List<CareerCenterStaff> staffMembers = new ArrayList<>();
        for (User user : users) {
            if (user instanceof CareerCenterStaff staff) {
                staffMembers.add(staff);
            }
        }
        return Collections.unmodifiableList(staffMembers);
    }

    /**
     * Loads students from a CSV file and registers them.
     *
     * @param file CSV file with student records
     */
    private void loadStudents(File file) {
        if (!isReadable(file)) {
            System.err.println("Student file missing: " + (file == null ? "null" : file.getPath()));
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] tokens = line.split(",");
                if (tokens.length < 4) {
                    System.err.println("Skipping malformed student row " + lineNo);
                    continue;
                }
                String id = tokens[0].trim();
                String name = tokens[1].trim();
                String major = tokens[2].trim();
                int year = parseInt(tokens[3].trim(), 1);
                if (!registerStudent(id, name, User.DEFAULT_PASSWORD, year, major)) {
                    System.err.println("Failed to register student at row " + lineNo + " (" + id + ")");
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load students", e);
        }
    }

    /**
     * Loads career center staff from a CSV file and registers them.
     *
     * @param file CSV file with staff records
     */
    private void loadStaff(File file) {
        if (!isReadable(file)) {
            System.err.println("Staff file missing: " + (file == null ? "null" : file.getPath()));
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] tokens = line.split(",");
                if (tokens.length < 4) {
                    System.err.println("Skipping malformed staff row " + lineNo);
                    continue;
                }
                String id = tokens[0].trim();
                String name = tokens[1].trim();
                String department = tokens[3].trim();
                if (!registerCareerCenterStaff(id, name, User.DEFAULT_PASSWORD, department)) {
                    System.err.println("Failed to register staff at row " + lineNo + " (" + id + ")");
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load staff", e);
        }
    }

    /**
     * Loads company representatives from a CSV file and registers them.
     *
     * @param file CSV file with representative records
     */
    private void loadCompanyRepresentatives(File file) {
        if (!isReadable(file)) {
            System.err.println("Company representative file missing: " + (file == null ? "null" : file.getPath()));
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] tokens = line.split(",");
                if (tokens.length < 6) {
                    System.err.println("Skipping malformed representative row " + lineNo);
                    continue;
                }
                String id = tokens[0].trim();
                String name = tokens[1].trim();
                String company = tokens[2].trim();
                String department = tokens[3].trim();
                String position = tokens[4].trim();
                boolean approved = tokens.length > 6 && Boolean.parseBoolean(tokens[6].trim());
                if (!registerCompanyRep(id, name, User.DEFAULT_PASSWORD, company, department, position, approved)) {
                    System.err.println("Failed to register representative at row " + lineNo + " (" + id + ")");
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load company representatives", e);
        }
    }

    /**
     * Adds a user to the system if the user is non-null and the ID is not already registered.
     *
     * @param user the user to add
     * @return true if added successfully, false otherwise
     */
    private boolean addUser(User user) {
        if (user == null || userExists(user.getUserID())) {
            return false;
        }
        users.add(user);
        return true;
    }

    /**
     * Checks if a user with the given ID already exists (case-insensitive).
     *
     * @param id user ID
     * @return true if the user exists, false otherwise
     */
    private boolean userExists(String id) {
        if (id == null) {
            return false;
        }
        for (User user : users) {
            if (user.getUserID().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a file is readable (non-null, exists, is a regular file, and readable).
     *
     * @param file the file to check
     * @return true if readable, false otherwise
     */
    private boolean isReadable(File file) {
        return file != null && file.exists() && file.isFile() && file.canRead();
    }

    /**
     * Parses a string into an integer, falling back to a default value if parsing fails.
     *
     * @param value    the string value to parse
     * @param fallback fallback value if parsing fails
     * @return parsed integer or fallback
     */
    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse number '" + value + "'. Using fallback " + fallback + ".");
            return fallback;
        }
    }

    /**
     * Finds a user by ID (case-insensitive).
     *
     * @param id user ID
     * @return the matching user or null if not found
     */
    public User findUserById(String id) {
        if (id == null) {
            return null;
        }
        for (User user : users) {
            if (user.getUserID().equalsIgnoreCase(id.trim())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Finds the latest pending account request associated with a representative ID.
     *
     * @param repId representative ID
     * @return matching {@link AccountRequest} or null if none found
     */
    private AccountRequest findRequest(String repId) {
        if (repId == null) {
            return null;
        }
        for (AccountRequest request : accountRequests) {
            if (request.getRep() != null && request.getRep().getUserID().equalsIgnoreCase(repId)
                    && AccountRequest.STATUS_PENDING.equals(request.getStatus())) {
                return request;
            }
        }
        return null;
    }

    /**
     * Finds the latest (most recent) account request for the given representative ID,
     * regardless of status.
     *
     * @param repId representative ID
     * @return the latest {@link AccountRequest} or null if none found
     */
    private AccountRequest findLatestRequestForRep(String repId) {
        if (repId == null) {
            return null;
        }
        for (int i = accountRequests.size() - 1; i >= 0; i--) {
            AccountRequest request = accountRequests.get(i);
            if (request.getRep() != null && request.getRep().getUserID().equalsIgnoreCase(repId)) {
                return request;
            }
        }
        return null;
    }
}
