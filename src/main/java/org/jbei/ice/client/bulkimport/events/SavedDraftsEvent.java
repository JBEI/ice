package org.jbei.ice.client.bulkimport.events;

import java.util.ArrayList;

import org.jbei.ice.shared.dto.BulkImportInfo;

import com.google.gwt.event.shared.GwtEvent;

public class SavedDraftsEvent extends GwtEvent<SavedDraftsEventHandler> {

    public static Type<SavedDraftsEventHandler> TYPE = new Type<SavedDraftsEventHandler>();
    private final ArrayList<BulkImportInfo> data;

    public SavedDraftsEvent(ArrayList<BulkImportInfo> data) {
        this.data = new ArrayList<BulkImportInfo>(data);
    }

    @Override
    public GwtEvent.Type<SavedDraftsEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SavedDraftsEventHandler handler) {
        handler.onDataRetrieval(this);
    }

    public ArrayList<BulkImportInfo> getData() {
        return data;
    }
}
