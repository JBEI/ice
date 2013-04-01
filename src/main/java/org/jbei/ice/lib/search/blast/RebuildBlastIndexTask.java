package org.jbei.ice.lib.search.blast;

import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.logging.Logger;

/**
 * Task to rebuild the blast index
 *
 * @author Hector Plahar
 */
public class RebuildBlastIndexTask extends Task {

    private final boolean force;

    public RebuildBlastIndexTask(boolean force) {
        this.force = force;
    }

    @Override
    public void execute() {
        Logger.info("Running blast rebuild task");
        Blast blast = new Blast();
        try {
            blast.rebuildDatabase(force);
        } catch (BlastException e) {
            Logger.error(e);
        }
    }
}
