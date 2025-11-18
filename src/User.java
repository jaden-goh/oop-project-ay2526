public abstract class User {
    public static final String DEFAULT_PASSWORD = "password";

    private final String userID;
    private String name;
    private String password;
    private FilterCriteria filterPreferences;

    protected User(String userID, String name, String password) {
        this.userID = userID;
        this.name = name;
        this.password = (password == null || password.isBlank()) ? DEFAULT_PASSWORD : password;
    }

    public String getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        System.out.println("Name updated to " + name);
    }

    public FilterCriteria getFilterPreferences() {
        return filterPreferences;
    }

    public void setFilterPreferences(FilterCriteria filterPreferences) {
        this.filterPreferences = filterPreferences;
        System.out.println("Filter preferences updated for user " + userID);
    }

    public boolean login(String password) {
        boolean success = this.password != null && this.password.equals(password);
        System.out.println(success ? "Login successful" : "Login failed");
        return success;
    }

    public void logout() {
        System.out.println("Logout for user " + userID);
    }

    public void changePassword(String newPass) {
        if (newPass == null || newPass.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (newPass.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        if (newPass.equals(this.password)) {
            throw new IllegalArgumentException("New password must differ from the old password.");
        }
        this.password = newPass;
        System.out.println("Password changed for user " + userID);
    }
}
