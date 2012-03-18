package org.jbei.ice.client.collection.event;

import java.util.ArrayList;

import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.event.shared.GwtEvent;

public class FolderRetrieveEvent extends GwtEvent<FolderRetrieveEventHandler> {

    public static Type<FolderRetrieveEventHandler> TYPE = new Type<FolderRetrieveEventHandler>();
    private final ArrayList<FolderDetails> items;

    public FolderRetrieveEvent(ArrayList<FolderDetails> items) {
        this.items = new ArrayList<FolderDetails>(items);
    }

    public FolderRetrieveEvent(FolderDetails item) {
        this.items = new ArrayList<FolderDetails>();
        this.items.add(item);
    }

    @Override
    public GwtEvent.Type<FolderRetrieveEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FolderRetrieveEventHandler handler) {
        handler.onFolderRetrieve(this);
    }

    public ArrayList<FolderDetails> getItems() {
        return items;
    }
}
