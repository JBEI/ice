package org.jbei.ice.client.collection;

import org.jbei.ice.client.Callback;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

/**
 * Used to transmit messages back and forth between the share collection widget
 *
 * @author Hector Plahar
 */
public class ShareCollectionData {

    private PermissionInfo info;
    private final Callback<PermissionInfo> infoCallback;
    private boolean isDelete;

    public ShareCollectionData(PermissionInfo info, Callback<PermissionInfo> callback) {
        this.info = info;
        this.infoCallback = callback;
    }

    public PermissionInfo getInfo() {
        return info;
    }

    public Callback<PermissionInfo> getInfoCallback() {
        return infoCallback;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }
}
