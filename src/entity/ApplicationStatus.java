// documentation done

package entity;

/**
 * Represents the possible statuses of a student's internship application.
 * Each status reflects a different stage of the application lifecycle.
 */
public enum ApplicationStatus {

    /** Application has been submitted and is awaiting review. */
    PENDING,

    /** Application has been approved by the company representative. */
    SUCCESSFUL,

    /** Application has been rejected by the company representative. */
    UNSUCCESSFUL,

    /** Student has requested or confirmed withdrawal from the application. */
    WITHDRAWN
}
