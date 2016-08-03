package org.jbei.ice.lib.executor;

/**
 * Represents the current status of a task that has been submitted to the ICE
 * task service
 *
 * @author Hector Plahar
 */
public enum TaskStatus {
    NEW,            // not submitted
    PENDING,        // submitted by waiting to be run
    IN_PROGRESS,    // task run in progress

    // terminal states
    COMPLETED,       // task run completed successfully
    EXCEPTION       // an exception caused the task to stop
}
