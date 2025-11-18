import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class UserManager {
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^U\\d{7}[A-Z]$");
    private static final Pattern STAFF_ID_PATTERN = Pattern.compile("^[A-Za-z]{3}\\d{3}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int DEFAULT_PENDING_PAGE_SIZE = 10;

    private final List<User> users = new ArrayList<>();
    private final List<AccountRequest> accountRequests = new ArrayList<>();
    private final List<String> bootstrapUserIds = new ArrayList<>();
    private boolean bootstrapMode;
    private String lastLoginMessage = "";

    public void loadAllUsers(File studentFile, File staffFile, File companyFile) {
        loadStudents(studentFile);
        loadStaff(staffFile);
        loadCompanyRepresentatives(companyFile);
    }

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
        User target = findUser(trimmedId);
        if (target == null) {
            lastLoginMessage = "No account was found for ID '" + trimmedId + "'. Use the registration option if needed.";
            return null;
        }
        if (!target.login(pass)) {
            lastLoginMessage = "Incorrect password. Use the \"reset\" keyword at the prompt to create a new one.";
            return null;
        }
        if (target instanceof CompanyRep rep && !rep.isApproved()) {
            lastLoginMessage = "Company representative account pending approval. Career Center staff will notify you once approved.";
            return null;
        }
        lastLoginMessage = "Login successful.";
        return target;
    }

    public String getLastLoginMessage() {
        return lastLoginMessage + " Need help? Contact the Career Center or use the reset prompts.";
    }

    public boolean resetPassword(String id, String newPassword) {
        User user = findUser(id);
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

    public List<AccountRequest> getPendingAccounts() {
        return getPendingAccounts(1, DEFAULT_PENDING_PAGE_SIZE, AccountRequest.STATUS_PENDING);
    }

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

    public List<CareerCenterStaff> getCareerCenterStaffMembers() {
        List<CareerCenterStaff> staffMembers = new ArrayList<>();
        for (User user : users) {
            if (user instanceof CareerCenterStaff staff) {
                staffMembers.add(staff);
            }
        }
        return Collections.unmodifiableList(staffMembers);
    }

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

    private boolean addUser(User user) {
        if (user == null || userExists(user.getUserID())) {
            return false;
        }
        users.add(user);
        if (bootstrapMode) {
            bootstrapUserIds.add(user.getUserID().toLowerCase(Locale.ROOT));
        }
        return true;
    }

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

    private boolean isReadable(File file) {
        return file != null && file.exists() && file.isFile() && file.canRead();
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse number '" + value + "'. Using fallback " + fallback + ".");
            return fallback;
        }
    }

    private User findUser(String id) {
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
}
