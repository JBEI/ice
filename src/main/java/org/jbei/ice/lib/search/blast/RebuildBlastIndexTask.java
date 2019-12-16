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

    @Override
    public void execute() {
        Logger.info("Running blast task with action: " + action.name());
        try {
            StandardBlastDatabase standardBlastDatabase = StandardBlastDatabase.getInstance();

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
