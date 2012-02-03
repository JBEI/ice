package org.jbei.ice.client.bulkimport.events;

import java.util.ArrayList;

import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.event.shared.GwtEvent;

public class SavedDraftsEvent extends GwtEvent<SavedDraftsEventHandler> {

    public static Type<SavedDraftsEventHandler> TYPE = new Type<SavedDraftsEventHandler>();
    private final ArrayList<BulkImportDraftInfo> data;

    public SavedDraftsEvent(ArrayList<BulkImportDraftInfo> data) {
        this.data = new ArrayList<BulkImportDraftInfo>(data);
    }

    @Override
    public GwtEvent.Type<SavedDraftsEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SavedDraftsEventHandler handler) {
        handler.onDataRetrieval(this);
    }

    public ArrayList<BulkImportDraftInfo> getData() {
        return data;
    }
}
