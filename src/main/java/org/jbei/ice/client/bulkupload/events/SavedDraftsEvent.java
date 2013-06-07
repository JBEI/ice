package org.jbei.ice.client.bulkupload.events;

import com.google.gwt.event.shared.GwtEvent;
import org.jbei.ice.shared.dto.BulkUploadInfo;

import java.util.ArrayList;

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
