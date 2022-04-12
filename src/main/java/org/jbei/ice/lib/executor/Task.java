package org.jbei.ice.lib.executor;

/**
 * Abstract class representing tasks that are run by the ice executor service
 *
 * @author Hector Plahar
 */
public abstract class Task {

    private TaskStatus status = TaskStatus.NEW;

    public abstract void execute();

    void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() {
        return this.status;
    }
}
