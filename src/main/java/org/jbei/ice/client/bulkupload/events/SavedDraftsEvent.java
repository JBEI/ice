package org.jbei.ice.client.bulkupload.events;

import java.util.ArrayList;

import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadInfo;

import com.google.gwt.event.shared.GwtEvent;

public class SavedDraftsEvent extends GwtEvent<SavedDraftsEventHandler> {

    public static final Type<SavedDraftsEventHandler> TYPE = new Type<SavedDraftsEventHandler>();
    private final ArrayList<BulkUploadInfo> data;

    public SavedDraftsEvent(ArrayList<BulkUploadInfo> data) {
        this.data = new ArrayList<BulkUploadInfo>(data);
    }

    @Override
    public GwtEvent.Type<SavedDraftsEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SavedDraftsEventHandler handler) {
        handler.onDataRetrieval(this);
    }

    public ArrayList<BulkUploadInfo> getData() {
        return data;
    }
}
