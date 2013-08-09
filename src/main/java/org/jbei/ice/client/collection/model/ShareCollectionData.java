package org.jbei.ice.client.collection.model;

import org.jbei.ice.client.Callback;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

/**
 * Used to transmit messages back and forth between the share collection widget
 * and the presenter that makes the service call to the server.
 *
 * @author Hector Plahar
 */
public class ShareCollectionData {

    private final AccessPermission access;
    private final Callback<ShareCollectionData> infoCallback;
    private final boolean isDelete;

    public ShareCollectionData(AccessPermission access, boolean isDelete, Callback<ShareCollectionData> callback) {
        this.access = access;
        this.infoCallback = callback;
        this.isDelete = isDelete;
    }

    public AccessPermission getAccess() {
        return access;
    }

    public Callback<ShareCollectionData> getInfoCallback() {
        return infoCallback;
    }

    public boolean isDelete() {
        return isDelete;
    }
}
