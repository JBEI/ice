package org.jbei.ice.lib.search.blast;

import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.logging.Logger;

/**
 * Task to rebuild the blast index
 *
 * @author Hector Plahar
 */
public class RebuildBlastIndexTask extends Task {

    @Override
    public void execute() {
        Logger.info("Running blast rebuild task");
        Blast blast = new Blast();
        try {
            blast.rebuildDatabase();
        } catch (BlastException e) {
            Logger.error(e);
        }
    }
}
