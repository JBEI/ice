package org.jbei.ice.lib.executor;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.hibernate.HibernateUtil;

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
        try {
            this.task.setStatus(TaskStatus.IN_PROGRESS);
            HibernateUtil.beginTransaction();
            task.execute();
            HibernateUtil.commitTransaction();
            this.task.setStatus(TaskStatus.COMPLETED);
        } catch (Throwable caught) {
            Logger.error(caught);
            HibernateUtil.rollbackTransaction();
            this.task.setStatus(TaskStatus.EXCEPTION);
        }
    }
}
