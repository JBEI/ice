package org.jbei.ice.lib.search.blast;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.executor.Task;

/**
 * Task to rebuild the blast index
 *
 * @author Hector Plahar
 */
public class RebuildBlastIndexTask extends Task {

    private final Action action;
    private final String partId;

    public RebuildBlastIndexTask(Action action, String partId) {
        this.action = action;
        this.partId = partId;
    }

    /**
     * Constructor for checking if the blast index exists and rebuilding if it doesn't
     */
    public RebuildBlastIndexTask() {
        this(Action.CHECK, null);
    }

    /**
     * Executes one of the following requested actions:
     * <p>
     * <code>CHECK</code>: check if database exists and if not rebuilds it <br>
     * <code>CREATE</code>: add a new sequence to the blast database<br>
     * <code>DELETE</code>: delete a sequence from the blast database<br>
     * <code>UPDATE</code>: update the blast database by removing and adding a sequence<br>
     * code>FORCE_REBUILD</code>: blow away existing blast database and create a new one from scratch
     * <p>
     * The blast database uses a lock file to prevent multiple concurrent actions that modify if
     */
    @Override
    public void execute() {
        Logger.info("Running blast task with action: " + action.name());
        try {
            StandardBlastDatabase standardBlastDatabase = StandardBlastDatabase.getInstance();
            if (standardBlastDatabase.isLocked()) { // todo : wait / notify
                Logger.info("Aborting run. Blast database is locked");
                return;
            }

            switch (this.action) {
                case CHECK:
                    standardBlastDatabase.checkRebuild(false);
                    break;

                case CREATE:
                    standardBlastDatabase.addSequence(this.partId);
                    break;

                case DELETE:
                    standardBlastDatabase.removeSequence(this.partId);
                    break;

                case UPDATE:
                    standardBlastDatabase.updateSequence(this.partId);
                    break;

                case FORCE_BUILD:
                    standardBlastDatabase.checkRebuild(true);
                    break;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
