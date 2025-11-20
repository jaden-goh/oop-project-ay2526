// documented

package entity;

/**
 * Represents a base abstract user in the internship management system.
 *
 * <p>All system users (Students, Company Representatives, Career Center Staff)
 * inherit from this class. The {@code User} class provides common attributes
 * such as user ID, name, password, and filter preferences, along with
 * shared behaviors such as login, logout, and password updates.</p>
 */

public abstract class User {

    /** Default password assigned to users if none is provided. */
    public static final String DEFAULT_PASSWORD = "password";

    /** Unique identifier of the user (e.g., NTU student ID, staff ID, or company email). */
    private final String userID;

    /** Name of the user. */
    private String name;

    /** Password used for login authentication. */
    private String password;

    /** Saved filter preferences for viewing internship listings. */
    private FilterCriteria filterPreferences;

    /**
     * Constructs a user with the given credentials.
     * If the provided password is null or blank, uses {@link #DEFAULT_PASSWORD}.
     *
     * @param userID   unique ID assigned to the user
     * @param name     user's name
     * @param password user's password (or default if invalid)
     */
    protected User(String userID, String name, String password) {
        this.userID = userID;
        this.name = name;
        this.password = (password == null || password.isBlank()) ? DEFAULT_PASSWORD : password;
    }

    /**
     * Returns the unique user ID.
     *
     * @return user ID string
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Returns the name of the user.
     *
     * @return user name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the user's name.
     *
     * @param name new user name
     */
    public void setName(String name) {
        this.name = name;
        System.out.println("Name updated to " + name);
    }

    /**
     * Returns the user's saved internship filter preferences.
     *
     * @return filter preferences, or null if none saved
     */
    public FilterCriteria getFilterPreferences() {
        return filterPreferences;
    }

    /**
     * Updates the user's saved internship filter preferences.
     *
     * @param filterPreferences new filter settings
     */
    public void setFilterPreferences(FilterCriteria filterPreferences) {
        this.filterPreferences = filterPreferences;
        System.out.println("Filter preferences updated for user " + userID);
    }

    /**
     * Attempts to log in using the given password.
     *
     * @param password attempted login password
     * @return true if login is successful; false otherwise
     */
    public boolean login(String password) {
        boolean success = this.password != null && this.password.equals(password);
        System.out.println(success ? "Login successful" : "Login failed");
        return success;
    }

    /**
     * Logs the user out of the system.
     */
    public void logout() {
        System.out.println("Logout for user " + userID);
    }

    /**
     * Changes the user's password.
     *
     * <p>Requirements:</p>
     * <ul>
     *     <li>Password must not be empty</li>
     *     <li>Password must be at least 8 characters</li>
     *     <li>Password must differ from the old password</li>
     * </ul>
     *
     * @param newPass the new password to set
     *
     * @throws IllegalArgumentException if the password is invalid
     */
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

    /**
     * Returns the user's current password.
     *
     * @return password string
     */
    public String getPassword() {
        return password;
    }

}
