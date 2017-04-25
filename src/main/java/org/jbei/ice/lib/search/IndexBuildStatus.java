package org.jbei.ice.lib.search;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class IndexBuildStatus implements IDataTransferModel {

    private final long done;
    private final long total;

    public IndexBuildStatus(long done, long total) {
        this.done = done;
        this.total = total;
    }

    public long getDone() {
        return this.done;
    }

    public long getTotal() {
        return this.total;
    }
}
