import java.time.LocalDateTime;

public class Notification {
    private final String message;
    private final LocalDateTime timestamp;

    public Notification(String message) {
        this(message, LocalDateTime.now());
    }

    public Notification(String message, LocalDateTime timestamp) {
        this.message = message == null ? "" : message.trim();
        this.timestamp = timestamp == null ? LocalDateTime.now() : timestamp;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return timestamp + " - " + message;
    }
}
