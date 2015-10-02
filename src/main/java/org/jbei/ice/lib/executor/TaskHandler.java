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
            HibernateUtil.beginTransaction();
            task.execute();
            HibernateUtil.commitTransaction();
        } catch (Throwable caught) {
            Logger.error(caught);
            HibernateUtil.rollbackTransaction();
        }
    }
}
