package org.jbei.ice.lib.executor;

/**
 * Runnable for running tasks
 *
 * @author Hector Plahar
 */
class TaskHandler implements Runnable {
    private final Task task;

    public TaskHandler(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        task.execute();
    }
}
