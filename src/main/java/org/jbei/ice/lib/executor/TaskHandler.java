package org.jbei.ice.lib.executor;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;

/**
 * Runnable for running tasks
 *
 * @author Hector Plahar
 */
class TaskHandler implements Runnable {

    private final Task task;

    TaskHandler(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        try {
            this.task.setStatus(TaskStatus.IN_PROGRESS);
            HibernateConfiguration.beginTransaction();
            task.execute();
            HibernateConfiguration.commitTransaction();
            this.task.setStatus(TaskStatus.COMPLETED);
        } catch (Throwable caught) {
            Logger.error(caught);
            HibernateConfiguration.rollbackTransaction();
            this.task.setStatus(TaskStatus.EXCEPTION);
        }
    }
}
