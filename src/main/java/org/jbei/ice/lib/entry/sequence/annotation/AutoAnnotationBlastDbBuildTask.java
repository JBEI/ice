package org.jbei.ice.lib.entry.sequence.annotation;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.search.blast.BlastPlus;

import java.io.IOException;

/**
 * Task to rebuild blast database of features for auto annotation
 * Runs one a day
 *
 * @author Hector Plahar
 */
public class AutoAnnotationBlastDbBuildTask extends Task {

    @Override
    public void execute() {
        Logger.info("Rebuilding auto annotation blast database");
        try {
            BlastPlus.rebuildFeaturesBlastDatabase("auto-annotation");
        } catch (IOException ioe) {
            Logger.error(ioe);
        }
    }
}
