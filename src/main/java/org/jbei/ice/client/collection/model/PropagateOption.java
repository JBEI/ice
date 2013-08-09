package org.jbei.ice.client.collection.model;

/**
 * wrapper around a boolean and id pairing that represents
 * whether the user has (de)selected the propagate option and for which folder
 *
 * @author Hector Plahar
 */
public class PropagateOption {

    private final boolean propagate;
    private final long folderId;

    public PropagateOption(boolean propagate, long folderId) {
        this.propagate = propagate;
        this.folderId = folderId;
    }

    public boolean isPropagate() {
        return propagate;
    }

    public long getFolderId() {
        return folderId;
    }
}
