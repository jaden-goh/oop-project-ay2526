// documented

package entity;

import java.time.LocalDateTime;

/**
 * Represents a notification message within the system.
 *
 * <p>A notification consists of a message and a timestamp marking when the
 * notification was generated. Notifications may be used for informing users
 * about updates such as application status changes, internship approvals,
 * or withdrawal decisions.</p>
 */

public class Notification {

    /** The content of the notification message. */
    private final String message;

    /** The timestamp of when the notification was created. */
    private final LocalDateTime timestamp;

    /**
     * Creates a notification with the current timestamp.
     *
     * @param message the text content of the notification
     */
    public Notification(String message) {
        this(message, LocalDateTime.now());
    }

    /**
     * Creates a notification with a custom timestamp.
     *
     * @param message   notification text; trimmed, or empty string if null
     * @param timestamp timestamp of the notification; defaults to now if null
     */
    public Notification(String message, LocalDateTime timestamp) {
        this.message = message == null ? "" : message.trim();
        this.timestamp = timestamp == null ? LocalDateTime.now() : timestamp;
    }

    /**
     * Returns the notification message.
     *
     * @return the message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the timestamp of the notification.
     *
     * @return timestamp of creation
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a string representation of the notification in the format:
     * <pre>timestamp - message</pre>
     *
     * @return formatted notification string
     */
    @Override
    public String toString() {
        return timestamp + " - " + message;
    }
}
