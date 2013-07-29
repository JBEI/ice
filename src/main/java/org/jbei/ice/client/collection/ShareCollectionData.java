package org.jbei.ice.client.collection;

import org.jbei.ice.client.Callback;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

/**
 * Used to transmit messages back and forth between the share collection widget
 *
 * @author Hector Plahar
 */
public class ShareCollectionData {

    private AccessPermission access;
    private final Callback<AccessPermission> infoCallback;
    private boolean isDelete;

    public ShareCollectionData(AccessPermission access, Callback<AccessPermission> callback) {
        this.access = access;
        this.infoCallback = callback;
    }

    public AccessPermission getAccess() {
        return access;
    }

    public Callback<AccessPermission> getInfoCallback() {
        return infoCallback;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }
}
